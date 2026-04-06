package com.fourm.discussion_forum.controller;

import com.fourm.discussion_forum.entity.*;
import com.fourm.discussion_forum.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public VoteController(VoteRepository voteRepository, UserRepository userRepository,
                          PostRepository postRepository, CommentRepository commentRepository) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<Map<String, Object>> votePost(@PathVariable Long postId,
                                                         @RequestBody Map<String, String> body,
                                                         Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        String voteTypeStr = body.get("voteType"); // "UPVOTE" or "DOWNVOTE"
        if (voteTypeStr == null || voteTypeStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voteType is required (UPVOTE or DOWNVOTE)");
        }
        Vote.VoteType voteType;
        try {
            voteType = Vote.VoteType.valueOf(voteTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid voteType: " + voteTypeStr);
        }

        Optional<Vote> existing = voteRepository.findByUserIdAndPostId(user.getId(), postId);

        if (existing.isPresent()) {
            Vote vote = existing.get();
            if (vote.getVoteType() == voteType) {
                // Remove vote (toggle off)
                if (voteType == Vote.VoteType.UPVOTE) post.setUpvotes(post.getUpvotes() - 1);
                else post.setDownvotes(post.getDownvotes() - 1);
                voteRepository.delete(vote);
            } else {
                // Switch vote
                if (voteType == Vote.VoteType.UPVOTE) { post.setUpvotes(post.getUpvotes() + 1); post.setDownvotes(post.getDownvotes() - 1); }
                else { post.setDownvotes(post.getDownvotes() + 1); post.setUpvotes(post.getUpvotes() - 1); }
                vote.setVoteType(voteType);
                voteRepository.save(vote);
            }
        } else {
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setPost(post);
            vote.setVoteType(voteType);
            voteRepository.save(vote);
            if (voteType == Vote.VoteType.UPVOTE) post.setUpvotes(post.getUpvotes() + 1);
            else post.setDownvotes(post.getDownvotes() + 1);
        }

        postRepository.save(post);
        return ResponseEntity.ok(Map.of("upvotes", post.getUpvotes(), "downvotes", post.getDownvotes()));
    }

    @PostMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, Object>> voteComment(@PathVariable Long commentId,
                                                            @RequestBody Map<String, String> body,
                                                            Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        String voteTypeStr = body.get("voteType");
        if (voteTypeStr == null || voteTypeStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voteType is required (UPVOTE or DOWNVOTE)");
        }
        Vote.VoteType voteType;
        try {
            voteType = Vote.VoteType.valueOf(voteTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid voteType: " + voteTypeStr);
        }

        Optional<Vote> existing = voteRepository.findByUserIdAndCommentId(user.getId(), commentId);

        if (existing.isPresent()) {
            Vote vote = existing.get();
            if (vote.getVoteType() == voteType) {
                if (voteType == Vote.VoteType.UPVOTE) comment.setUpvotes(comment.getUpvotes() - 1);
                else comment.setDownvotes(comment.getDownvotes() - 1);
                voteRepository.delete(vote);
            } else {
                if (voteType == Vote.VoteType.UPVOTE) { comment.setUpvotes(comment.getUpvotes() + 1); comment.setDownvotes(comment.getDownvotes() - 1); }
                else { comment.setDownvotes(comment.getDownvotes() + 1); comment.setUpvotes(comment.getUpvotes() - 1); }
                vote.setVoteType(voteType);
                voteRepository.save(vote);
            }
        } else {
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setComment(comment);
            vote.setVoteType(voteType);
            voteRepository.save(vote);
            if (voteType == Vote.VoteType.UPVOTE) comment.setUpvotes(comment.getUpvotes() + 1);
            else comment.setDownvotes(comment.getDownvotes() + 1);
        }

        commentRepository.save(comment);
        return ResponseEntity.ok(Map.of("upvotes", comment.getUpvotes(), "downvotes", comment.getDownvotes()));
    }
}
