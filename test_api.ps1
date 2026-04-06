$baseUrl = "http://localhost:8082/api"

# 1. Register
Write-Host "1. Registering user..."
$registerBody = @{
    name = "Test User"
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json
$registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
Write-Host "Register Response: $($registerResponse | ConvertTo-Json)"

# 2. Login
Write-Host "`n2. Logging in..."
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token
Write-Host "Login successful. Token: $($token.Substring(0, 20))..."

# 3. Create Thread
Write-Host "`n3. Creating thread..."
$threadBody = @{
    title = "Hello Forum"
    content = "This is my first thread content."
} | ConvertTo-Json
$threadResponse = Invoke-RestMethod -Uri "$baseUrl/threads" -Method Post -Body $threadBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $token" }
$threadId = $threadResponse.id
Write-Host "Thread created with ID: $threadId"

# 4. Get All Threads
Write-Host "`n4. Getting all threads..."
$threads = Invoke-RestMethod -Uri "$baseUrl/threads" -Method Get
Write-Host "Total threads: $($threads.Count)"

# 5. Summarize Thread
Write-Host "`n5. Summarizing thread $threadId..."
$summarizeResponse = Invoke-RestMethod -Uri "$baseUrl/threads/$threadId/summarize" -Method Post -Headers @{ Authorization = "Bearer $token" }
Write-Host "Summary: $($summarizeResponse.summary)"

# 6. Create Reply
Write-Host "`n6. Creating reply for thread $threadId..."
$replyBody = @{
    content = "This is a reply to the first thread."
} | ConvertTo-Json
$replyResponse = Invoke-RestMethod -Uri "$baseUrl/threads/$threadId/replies" -Method Post -Body $replyBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $token" }
$replyId = $replyResponse.id
Write-Host "Reply created with ID: $replyId"

# 7. Like Reply
Write-Host "`n7. Liking reply $replyId..."
$likeResponse = Invoke-WebRequest -Uri "$baseUrl/replies/$replyId/like" -Method Post -Headers @{ Authorization = "Bearer $token" }
Write-Host "Like status code: $($likeResponse.StatusCode)"

# 8. Get Replies
Write-Host "`n8. Getting replies for thread $threadId..."
$replies = Invoke-RestMethod -Uri "$baseUrl/threads/$threadId/replies" -Method Get
foreach ($reply in $replies) {
    Write-Host "Reply content: $($reply.content) | Likes: $($reply.likes)"
}
