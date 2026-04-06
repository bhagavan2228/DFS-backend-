<#
.SYNOPSIS
  End-to-end API checks against a running Spring Boot instance + MySQL persistence checks.

.PARAMETER BaseUrl
  API root including /api, e.g. http://localhost:8082/api

.EXAMPLE
  .\scripts\integration-test.ps1
  .\scripts\integration-test.ps1 -BaseUrl "https://api.example.com/api"
#>
param(
    [string]$BaseUrl = "http://localhost:8082/api"
)

$ErrorActionPreference = "Stop"
$passed = 0
$failed = 0

function Assert-Ok {
    param([string]$Name, [scriptblock]$Fn)
    try {
        & $Fn
        Write-Host "  [PASS] $Name" -ForegroundColor Green
        $script:passed++
    } catch {
        Write-Host "  [FAIL] $Name - $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
}

function Invoke-ApiJson {
    param(
        [string]$Method,
        [string]$Path,
        $Body = $null,
        [hashtable]$Headers = @{}
    )
    $uri = "$BaseUrl$Path"
    $params = @{
        Uri = $uri
        Method = $Method
        Headers = $Headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 8 -Compress)
    }
    return Invoke-RestMethod @params
}

function Invoke-ApiRaw {
    param(
        [string]$Method,
        [string]$Path,
        $Body = $null,
        [hashtable]$Headers = @{}
    )
    $uri = "$BaseUrl$Path"
    $params = @{
        Uri = $uri
        Method = $Method
        Headers = $Headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 8 -Compress)
    }
    return Invoke-WebRequest @params -UseBasicParsing
}

$ts = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$email1 = "inttest1_$ts@local.test"
$email2 = "inttest2_$ts@local.test"
$name1 = "IntTest One $ts"
$name2 = "IntTest Two $ts"
$password = "TestPass123!"

Write-Host "`n=== Discussion Forum API integration tests ===" -ForegroundColor Cyan
Write-Host "Base: $BaseUrl`n"

# --- Public / infra ---
Assert-Ok "GET actuator health (no /api)" {
    $root = $BaseUrl -replace '/api$', ''
    $h = Invoke-RestMethod -Uri "$root/actuator/health" -Method Get
    if ($h.status -ne "UP") { throw "status not UP: $($h | ConvertTo-Json)" }
}

Assert-Ok "GET /threads (public)" {
    $t = Invoke-ApiJson -Method Get -Path "/threads"
    if ($null -eq $t) { throw "null response" }
}

Assert-Ok "GET /categories (public)" {
    Invoke-ApiJson -Method Get -Path "/categories" | Out-Null
}

Assert-Ok "GET /posts (public)" {
    Invoke-ApiJson -Method Get -Path "/posts" | Out-Null
}

# --- User 1 ---
Assert-Ok "POST /auth/register (user1)" {
    Invoke-ApiJson -Method Post -Path "/auth/register" -Body @{
        name = $name1
        email = $email1
        password = $password
    } | Out-Null
}

Assert-Ok "POST /auth/login (user1) returns token + role" {
    $script:login1 = Invoke-ApiJson -Method Post -Path "/auth/login" -Body @{
        email = $email1
        password = $password
    }
    if (-not $script:login1.token) { throw "missing token" }
    if (-not $script:login1.user.role) { throw "missing user.role" }
    $script:token1 = $script:login1.token
}

Assert-Ok "GET /users/me (user1)" {
    $me = Invoke-ApiJson -Method Get -Path "/users/me" -Headers @{ Authorization = "Bearer $($script:token1)" }
    if ($me.email -ne $email1) { throw "email mismatch" }
}

Assert-Ok 'POST /users/me/toggle-ghost (round-trip; restores initial state)' {
    $before = Invoke-ApiJson -Method Get -Path "/users/me" -Headers @{ Authorization = "Bearer $($script:token1)" }
    Invoke-ApiJson -Method Post -Path "/users/me/toggle-ghost" -Headers @{ Authorization = "Bearer $($script:token1)" } | Out-Null
    $mid = Invoke-ApiJson -Method Get -Path "/users/me" -Headers @{ Authorization = "Bearer $($script:token1)" }
    Invoke-ApiJson -Method Post -Path "/users/me/toggle-ghost" -Headers @{ Authorization = "Bearer $($script:token1)" } | Out-Null
    $after = Invoke-ApiJson -Method Get -Path "/users/me" -Headers @{ Authorization = "Bearer $($script:token1)" }
    if ($after.ghostMode -ne $before.ghostMode) { throw "ghostMode not restored after double toggle" }
    if ($mid.ghostMode -eq $before.ghostMode) { throw "expected middle state to differ" }
}

# --- Thread + persistence ---
$threadTitle = "Integration thread $ts"
Assert-Ok "POST /threads creates row (user1)" {
    $script:thread = Invoke-ApiJson -Method Post -Path "/threads" -Headers @{ Authorization = "Bearer $($script:token1)" } -Body @{
        title = $threadTitle
        content = "Body content for persistence check $ts"
    }
    if (-not $script:thread.id) { throw "no thread id" }
    $script:threadId = $script:thread.id
}

Assert-Ok "GET /threads/{id} matches DB (title)" {
    $g = Invoke-ApiJson -Method Get -Path "/threads/$($script:threadId)"
    if ($g.title -ne $threadTitle) { throw "title mismatch" }
}

Assert-Ok "GET /threads list includes new thread (persisted)" {
    $all = Invoke-ApiJson -Method Get -Path "/threads"
    $found = $all | Where-Object { $_.id -eq $script:threadId }
    if (-not $found) { throw "thread not in list" }
}

Assert-Ok "GET /threads/check-duplicate" {
    $d = Invoke-ApiJson -Method Get -Path "/threads/check-duplicate?title=$([uri]::EscapeDataString($threadTitle))"
    if ($null -eq $d) { throw "null" }
}

Assert-Ok "POST /threads/{id}/summarize" {
    $s = Invoke-ApiJson -Method Post -Path "/threads/$($script:threadId)/summarize" -Headers @{ Authorization = "Bearer $($script:token1)" }
    if (-not $s.summary) { throw "no summary" }
}

Assert-Ok "POST /threads/{id}/replies (user1)" {
    $script:reply1 = Invoke-ApiJson -Method Post -Path "/threads/$($script:threadId)/replies" -Headers @{ Authorization = "Bearer $($script:token1)" } -Body @{
        content = "First reply $ts"
    }
    if (-not $script:reply1.id) { throw "no reply id" }
    $script:replyId = $script:reply1.id
}

Assert-Ok "GET /threads/{id}/replies includes reply (persisted)" {
    $rs = Invoke-ApiJson -Method Get -Path "/threads/$($script:threadId)/replies"
    $hit = $rs | Where-Object { $_.id -eq $script:replyId }
    if (-not $hit) { throw "reply missing" }
}

Assert-Ok "POST /replies/{id}/like" {
    $lr = Invoke-ApiJson -Method Post -Path "/replies/$($script:replyId)/like" -Headers @{ Authorization = "Bearer $($script:token1)" }
    if ($null -eq $lr.likes) { throw "no likes in response" }
}

Assert-Ok "POST /reports (THREAD)" {
    Invoke-ApiRaw -Method Post -Path "/reports" -Headers @{ Authorization = "Bearer $($script:token1)" } -Body @{
        targetType = "THREAD"
        targetId = [long]$script:threadId
        reason = "integration test report"
    } | Out-Null
}

# --- User 2: reply -> notification to user1 ---
Assert-Ok "POST /auth/register (user2)" {
    Invoke-ApiJson -Method Post -Path "/auth/register" -Body @{
        name = $name2
        email = $email2
        password = $password
    } | Out-Null
}

Assert-Ok "POST /auth/login (user2)" {
    $l2 = Invoke-ApiJson -Method Post -Path "/auth/login" -Body @{
        email = $email2
        password = $password
    }
    $script:token2 = $l2.token
}

Assert-Ok "POST reply as user2 (triggers notification)" {
    Invoke-ApiJson -Method Post -Path "/threads/$($script:threadId)/replies" -Headers @{ Authorization = "Bearer $($script:token2)" } -Body @{
        content = "Reply from second user $ts"
    } | Out-Null
}

Assert-Ok 'GET /notifications/my (user1) notification stored' {
    Start-Sleep -Milliseconds 400
    $ns = Invoke-ApiJson -Method Get -Path "/notifications/my" -Headers @{ Authorization = "Bearer $($script:token1)" }
    if ($ns.Count -lt 1) { throw "expected at least one notification" }
    $script:notifId = $ns[0].id
}

Assert-Ok "PATCH /notifications/{id}/read" {
    Invoke-ApiJson -Method Patch -Path "/notifications/$($script:notifId)/read" -Headers @{ Authorization = "Bearer $($script:token1)" } | Out-Null
}

# --- Posts / votes / saved (requires category) ---
Assert-Ok "Ensure category exists for post test" {
    $cats = Invoke-ApiJson -Method Get -Path "/categories"
    if ($cats.Count -gt 0) {
        $script:catId = $cats[0].id
    } else {
        $c = Invoke-ApiJson -Method Post -Path "/categories" -Headers @{ Authorization = "Bearer $($script:token1)" } -Body @{
            name = "IntegrationCat $ts"
        }
        $script:catId = $c.id
    }
}

Assert-Ok "POST /posts persists" {
    $script:post = Invoke-ApiJson -Method Post -Path "/posts" -Headers @{ Authorization = "Bearer $($script:token1)" } -Body @{
        title = "Integration post $ts"
        description = "Post body $ts"
        categoryId = $script:catId
        tags = @("integration", "test")
    }
    if (-not $script:post.id) { throw "no post id" }
    $script:postId = $script:post.id
}

Assert-Ok "GET /posts/{id} persisted title" {
    $p = Invoke-ApiJson -Method Get -Path "/posts/$($script:postId)"
    if ($p.title -notlike "*$ts*") { throw "title not found" }
}

Assert-Ok "POST /posts/{id}/comments" {
    $script:comment = Invoke-ApiJson -Method Post -Path "/posts/$($script:postId)/comments" -Headers @{ Authorization = "Bearer $($script:token1)" } -Body @{
        content = "Comment $ts"
    }
    if (-not $script:comment.id) { throw "no comment id" }
    $script:commentId = $script:comment.id
}

Assert-Ok "GET /posts/{id}/comments lists comment" {
    $cs = Invoke-ApiJson -Method Get -Path "/posts/$($script:postId)/comments"
    $c = $cs | Where-Object { $_.id -eq $script:commentId }
    if (-not $c) { throw "comment missing" }
}

Assert-Ok "POST /votes/post/{id}" {
    Invoke-ApiJson -Method Post -Path "/votes/post/$($script:postId)" -Headers @{ Authorization = "Bearer $($script:token1)" } -Body @{
        voteType = "UPVOTE"
    } | Out-Null
}

Assert-Ok "POST /votes/comment/{id}" {
    Invoke-ApiJson -Method Post -Path "/votes/comment/$($script:commentId)" -Headers @{ Authorization = "Bearer $($script:token1)" } -Body @{
        voteType = "UPVOTE"
    } | Out-Null
}

Assert-Ok "POST /saved-posts/{postId} + GET /saved-posts/my" {
    Invoke-ApiJson -Method Post -Path "/saved-posts/$($script:postId)" -Headers @{ Authorization = "Bearer $($script:token1)" } | Out-Null
    $saved = Invoke-ApiJson -Method Get -Path "/saved-posts/my" -Headers @{ Authorization = "Bearer $($script:token1)" }
    $hit = $saved | Where-Object { $_.post.id -eq $script:postId }
    if (-not $hit) { throw "saved post not listed" }
}

Assert-Ok "DELETE /saved-posts/{postId}" {
    Invoke-ApiRaw -Method Delete -Path "/saved-posts/$($script:postId)" -Headers @{ Authorization = "Bearer $($script:token1)" } | Out-Null
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "Passed: $passed"
Write-Host "Failed: $failed"
if ($failed -gt 0) {
    exit 1
}
exit 0
