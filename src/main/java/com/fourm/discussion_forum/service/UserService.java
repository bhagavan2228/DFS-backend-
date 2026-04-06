package com.fourm.discussion_forum.service;

import com.fourm.discussion_forum.dto.UserDto;
import com.fourm.discussion_forum.entity.User;
import com.fourm.discussion_forum.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto toggleGhostMode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setGhostMode(!user.getGhostMode());
        user = userRepository.save(user);

        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getGhostMode(),
                user.getRole() != null ? user.getRole().name() : "USER");
    }

    public UserDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getGhostMode(),
                user.getRole() != null ? user.getRole().name() : "USER");
    }
}
