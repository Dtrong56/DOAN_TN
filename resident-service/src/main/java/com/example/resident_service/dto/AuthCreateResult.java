package com.example.resident_service.dto;

import lombok.Data;

@Data
public class AuthCreateResult {
    private String cccd;
    private String userId; // null if failed
    private String error;  // null if success
}
