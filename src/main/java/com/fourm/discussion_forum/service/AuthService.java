package com.fourm.discussion_forum.service;

import com.fourm.discussion_forum.dto.AuthRequest;
import com.fourm.discussion_forum.dto.AuthResponse;
import com.fourm.discussion_forum.dto.RegisterRequest;
import com.fourm.discussion_forum.dto.UserDto;
import com.fourm.discussion_forum.entity.User;
import com.fourm.discussion_forum.repository.UserRepository;
import com.fourm.discussion_forum.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    public UserDto register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getGhostMode(),
                user.getRole() != null ? user.getRole().name() : "USER");
    }

    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String jwtToken = jwtUtils.generateToken(user.getEmail());

        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getGhostMode(),
                user.getRole() != null ? user.getRole().name() : "USER");

        return new AuthResponse(jwtToken, userDto);
    }
}
