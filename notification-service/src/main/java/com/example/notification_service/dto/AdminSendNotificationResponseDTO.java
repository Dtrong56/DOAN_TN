package com.example.notification_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminSendNotificationResponseDTO {
    private boolean success;
    private String message;

    // Map<userId, status>  → SENT / FAILED
    private Map<String, String> results;
}
