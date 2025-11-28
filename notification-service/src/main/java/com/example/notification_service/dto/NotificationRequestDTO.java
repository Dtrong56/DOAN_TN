package com.example.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    // Đối tượng nhận thông báo
    private String tenantId;
    private String userId;
    private String recipientEmail; // nếu gửi email

    // Nội dung thông báo
    private String title;
    private String message;

    // Loại thông báo (INVOICE, REMINDER, CONTRACT, SYSTEM, CUSTOM)
    private String type;

    // Đối tượng nghiệp vụ liên quan
    private String objectType; // Invoice, Contract, ServiceAppendix...
    private String objectId;

    // Hành động nghiệp vụ (APPENDIX_EXPIRED, INVOICE_CREATED...)
    private String action;

    // Kênh gửi (EMAIL, SYSTEM) — mặc định là EMAIL trong demo
    private String channelType;

    // Metadata bổ sung (nếu cần)
    private Map<String, Object> metadata;
}
