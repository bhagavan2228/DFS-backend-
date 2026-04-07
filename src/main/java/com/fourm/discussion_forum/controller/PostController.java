package com.fourm.discussion_forum.controller;

import com.fourm.discussion_forum.entity.Post;
import com.fourm.discussion_forum.entity.Category;
import com.fourm.discussion_forum.entity.User;
import com.fourm.discussion_forum.repository.PostRepository;
import com.fourm.discussion_forum.repository.CategoryRepository;
import com.fourm.discussion_forum.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public PostController(PostRepository postRepository, UserRepository userRepository,
                          CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAll() {
        return ResponseEntity.ok(postRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getById(@PathVariable Long id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID cannot be null");
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Post>> getByCategory(@PathVariable Long categoryId) {
        if (categoryId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID cannot be null");
        return ResponseEntity.ok(postRepository.findByCategoryId(categoryId));
    }

    @PostMapping
    public ResponseEntity<Post> create(@RequestBody Map<String, Object> body, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Post post = new Post();
        post.setTitle((String) body.get("title"));
        post.setDescription((String) body.get("description"));
        post.setUser(user);

        if (body.containsKey("categoryId") && body.get("categoryId") != null) {
            long catId = Long.parseLong(body.get("categoryId").toString());
            Category category = categoryRepository.findById(catId).orElse(null);
            post.setCategory(category);
        }

        if (body.containsKey("tags") && body.get("tags") instanceof List) {
            List<?> rawTags = (List<?>) body.get("tags");
            List<String> tags = new java.util.ArrayList<>();
            for (Object obj : rawTags) {
                if (obj instanceof String) {
                    tags.add((String) obj);
                }
            }
            post.setTags(tags);
        }

        @SuppressWarnings("null")
        Post savedPost = postRepository.save(post);
        return ResponseEntity.ok(savedPost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> update(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID cannot be null");
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (body.containsKey("title")) post.setTitle((String) body.get("title"));
        if (body.containsKey("description")) post.setDescription((String) body.get("description"));

        @SuppressWarnings("null")
        Post savedPost = postRepository.save(post);
        return ResponseEntity.ok(savedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (id == null) return ResponseEntity.badRequest().build();
        postRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
