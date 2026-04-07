package com.fourm.discussion_forum.controller;

import com.fourm.discussion_forum.entity.Comment;
import com.fourm.discussion_forum.entity.Post;
import com.fourm.discussion_forum.entity.User;
import com.fourm.discussion_forum.repository.CommentRepository;
import com.fourm.discussion_forum.repository.PostRepository;
import com.fourm.discussion_forum.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentController(CommentRepository commentRepository, PostRepository postRepository,
                             UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getByPost(@PathVariable Long postId) {
        if (postId == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(commentRepository.findByPostId(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Comment> create(@PathVariable Long postId,
                                          @RequestBody Map<String, Object> body,
                                          Authentication auth) {
        if (postId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID cannot be null");
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Comment comment = new Comment();
        comment.setContent((String) body.get("content"));
        comment.setUser(user);
        comment.setPost(post);

        if (body.containsKey("parentCommentId") && body.get("parentCommentId") != null) {
            long parentId = Long.parseLong(body.get("parentCommentId").toString());
            Comment parent = commentRepository.findById(parentId).orElse(null);
            comment.setParentComment(parent);
        }

        return ResponseEntity.ok(commentRepository.save(comment));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (id == null) return ResponseEntity.badRequest().build();
        commentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
