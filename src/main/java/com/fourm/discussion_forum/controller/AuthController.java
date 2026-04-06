package com.fourm.discussion_forum.controller;

import com.fourm.discussion_forum.dto.AuthRequest;
import com.fourm.discussion_forum.dto.AuthResponse;
import com.fourm.discussion_forum.dto.RegisterRequest;
import com.fourm.discussion_forum.dto.UserDto;
import com.fourm.discussion_forum.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
