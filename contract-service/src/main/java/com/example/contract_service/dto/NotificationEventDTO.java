package com.example.contract_service.dto;

import lombok.Data;

@Data
public class NotificationEventDTO {
    private String type;     // e.g. "NEW_SERVICE_REGISTRATION"
    private String tenantId;
    private String title;
    private String content;
    private String targetRole;
}

