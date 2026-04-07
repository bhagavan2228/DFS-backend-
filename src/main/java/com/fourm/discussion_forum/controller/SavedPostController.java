package com.fourm.discussion_forum.controller;

import com.fourm.discussion_forum.entity.Post;
import com.fourm.discussion_forum.entity.SavedPost;
import com.fourm.discussion_forum.entity.User;
import com.fourm.discussion_forum.repository.PostRepository;
import com.fourm.discussion_forum.repository.SavedPostRepository;
import com.fourm.discussion_forum.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/saved-posts")
public class SavedPostController {

    private final SavedPostRepository savedPostRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public SavedPostController(SavedPostRepository savedPostRepository, UserRepository userRepository,
                               PostRepository postRepository) {
        this.savedPostRepository = savedPostRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<List<SavedPost>> getMySavedPosts(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(savedPostRepository.findByUserId(user.getId()));
    }

    @PostMapping("/{postId}")
    public ResponseEntity<SavedPost> savePost(@PathVariable Long postId, Authentication auth) {
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID cannot be null");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (savedPostRepository.findByUserIdAndPostId(user.getId(), postId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Post already saved");
        }

        SavedPost savedPost = new SavedPost();
        savedPost.setUser(user);
        savedPost.setPost(post);
        return ResponseEntity.ok(savedPostRepository.save(savedPost));
    }

    @DeleteMapping("/{postId}")
    @Transactional
    public ResponseEntity<Void> unsavePost(@PathVariable Long postId, Authentication auth) {
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID cannot be null");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        savedPostRepository.deleteByUserIdAndPostId(user.getId(), postId);
        return ResponseEntity.ok().build();
    }
}
