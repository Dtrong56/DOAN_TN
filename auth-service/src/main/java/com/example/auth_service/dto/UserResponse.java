package com.example.auth_service.dto;

public class UserResponse {
    private String userId;
    private String username;
    private boolean active;
    private String message;

    public UserResponse(String userId, String username, boolean active, String message) {
        this.userId = userId;
        this.username = username;
        this.active = active;
        this.message = message;
    }

    // getters & setters
}
