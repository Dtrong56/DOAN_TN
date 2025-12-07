package com.example.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {

    private String notificationId;
    private String logId;

    // QUEUED / FAILED
    private String status;

    // email của người nhận (nếu có)
    private String email;

    // mô tả cho service gọi biết chuyện gì đang diễn ra
    private String message;

    private LocalDateTime timestamp;
}
