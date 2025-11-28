package com.example.resident_service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class NotificationRequestDTO {
    private String tenantId;
    private String userId;
    private String recipientEmail;
    private String title;
    private String message;
    private String type;
    private String objectType;
    private String objectId;
    private String action;
    private String channelType;
    private Map<String, Object> metadata;
}
