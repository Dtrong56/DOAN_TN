package com.example.auth_service.dto;

import lombok.Data;

@Data
public class ValidateTokenRequest {
    private String token;
}
