package com.example.auth_service.dto;

import lombok.Data;

@Data
public class AuthUserCreateRequest {
    private String fullName;
    private String cccd;
    private String phone;
    private String email;
    private String tenantId; // BẮT BUỘC, để biết cư dân thuộc tenant nào
}
