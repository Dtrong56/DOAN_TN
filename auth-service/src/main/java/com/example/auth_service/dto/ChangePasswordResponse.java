package com.example.auth_service.dto;

public class ChangePasswordResponse {
    private String message;

    public ChangePasswordResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
}
