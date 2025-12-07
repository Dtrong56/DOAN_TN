package com.example.notification_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminSendNotificationRequestDTO {
    private List<String> residentIds;  // Cư dân cần gửi
    private String title;
    private String message;
}

