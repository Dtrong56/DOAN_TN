package com.example.auth_service.dto;

import lombok.Data;

@Data
public class AuthCreateResult {
    private String cccd;
    private String userId;
    private String error; // null nếu success

    public AuthCreateResult(String cccd, String userId, String error) {
        this.cccd = cccd;
        this.userId = userId;
        this.error = error;
    }
}
