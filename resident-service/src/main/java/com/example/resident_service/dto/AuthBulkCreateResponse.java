package com.example.resident_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class AuthBulkCreateResponse {
    private boolean success;
    private List<AuthCreateResult> results;
}
