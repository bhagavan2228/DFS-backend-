package com.fourm.discussion_forum.dto;

public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Boolean ghostMode;
    /** USER or ADMIN */
    private String role;

    public UserDto(Long id, String name, String email, Boolean ghostMode, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.ghostMode = ghostMode;
        this.role = role;
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getGhostMode() {
        return ghostMode;
    }

    public void setGhostMode(Boolean ghostMode) {
        this.ghostMode = ghostMode;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
