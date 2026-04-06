package com.fourm.discussion_forum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Maps to the 'name' column
    @Column(nullable = false)
    private String name;

    // Maps to the 'username' column (we'll use email prefix as username)
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    // Extra columns in dfs.users that need defaults
    @Column(name = "reputation_points", nullable = false)
    private Integer reputationPoints = 0;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "bio")
    private String bio;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "ghost_mode", nullable = false)
    private Boolean ghostMode = false;

    public enum UserRole {
        USER, ADMIN
    }

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (reputationPoints == null) reputationPoints = 0;
        if (role == null) role = UserRole.USER;
        if (ghostMode == null) ghostMode = false;
        // auto-generate username from email if not set
        if (username == null && email != null) {
            username = email.split("@")[0] + "_" + System.currentTimeMillis() % 10000;
        }
    }

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getReputationPoints() { return reputationPoints; }
    public void setReputationPoints(Integer reputationPoints) { this.reputationPoints = reputationPoints; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getGhostMode() { return ghostMode; }
    public void setGhostMode(Boolean ghostMode) { this.ghostMode = ghostMode; }
}
