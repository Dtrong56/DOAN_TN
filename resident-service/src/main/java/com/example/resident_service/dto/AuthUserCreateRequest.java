package com.example.resident_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthUserCreateRequest {
    private String fullName;
    private String cccd;
    private String email;
    private String phone;
    private String tenantId;
    // any default role e.g. "RESIDENT" can be set on auth-service side or pass here
}
