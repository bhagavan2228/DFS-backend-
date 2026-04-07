package com.fourm.discussion_forum.controller;

import com.fourm.discussion_forum.dto.SummaryResponse;
import com.fourm.discussion_forum.dto.ThreadRequest;
import com.fourm.discussion_forum.entity.Thread;
import com.fourm.discussion_forum.service.ThreadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.lang.NonNull;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {

    private final ThreadService threadService;

    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @GetMapping
    public ResponseEntity<List<Thread>> getAllThreads() {
        return ResponseEntity.ok(threadService.getAllThreads());
    }

    @PostMapping
    public ResponseEntity<Thread> createThread(@RequestBody ThreadRequest request, Authentication authentication) {
        return ResponseEntity.ok(threadService.createThread(request, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Thread> getThreadById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(threadService.getThreadById(id));
    }

    @PostMapping("/{id}/summarize")
    public ResponseEntity<SummaryResponse> summarizeThread(@PathVariable @NonNull Long id) {
        String summary = threadService.summarizeThread(id);
        return ResponseEntity.ok(new SummaryResponse(summary));
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<List<Thread>> checkDuplicate(@RequestParam String title) {
        return ResponseEntity.ok(threadService.getSimilarThreads(title));
    }
}
