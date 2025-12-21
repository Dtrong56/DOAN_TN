package com.example.auth_service.dto;

import lombok.Data;

@Data
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

    public UserResponse() {} // thêm constructor rỗng
}
