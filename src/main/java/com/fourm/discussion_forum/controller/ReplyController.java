package com.fourm.discussion_forum.controller;

import com.fourm.discussion_forum.dto.ReplyRequest;
import com.fourm.discussion_forum.entity.Reply;
import com.fourm.discussion_forum.service.ReplyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReplyController {

    private final ReplyService replyService;

    public ReplyController(ReplyService replyService) {
        this.replyService = replyService;
    }

    @GetMapping("/threads/{id}/replies")
    public ResponseEntity<List<Reply>> getRepliesByThreadId(@PathVariable("id") Long threadId) {
        return ResponseEntity.ok(replyService.getRepliesByThreadId(threadId));
    }

    @PostMapping("/threads/{id}/replies")
    public ResponseEntity<Reply> createReply(@PathVariable("id") Long threadId, 
                                             @RequestBody ReplyRequest request, 
                                             Authentication authentication) {
        return ResponseEntity.ok(replyService.createReply(threadId, request, authentication.getName()));
    }

    @PostMapping("/replies/{replyId}/like")
    public ResponseEntity<java.util.Map<String, Object>> likeReply(
            @PathVariable Long replyId,
            Authentication authentication) {
        return ResponseEntity.ok(replyService.toggleLike(replyId, authentication.getName()));
    }
}
