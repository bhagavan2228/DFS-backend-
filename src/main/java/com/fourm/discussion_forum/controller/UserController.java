package com.fourm.discussion_forum.controller;

import com.fourm.discussion_forum.dto.UserDto;
import com.fourm.discussion_forum.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(authentication.getName()));
    }

    @PostMapping("/me/toggle-ghost")
    public ResponseEntity<UserDto> toggleGhostMode(Authentication authentication) {
        return ResponseEntity.ok(userService.toggleGhostMode(authentication.getName()));
    }
}
