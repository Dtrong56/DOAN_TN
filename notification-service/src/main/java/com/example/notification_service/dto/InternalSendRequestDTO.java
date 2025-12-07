package com.example.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalSendRequestDTO {
    private String tenantId;
    private String residentId;
    private String type;
    private String message;
}

