package com.example.notification_service.controller;

import com.example.notification_service.dto.AdminSendNotificationRequestDTO;
import com.example.notification_service.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.notification_service.security.TenantContext;
import com.example.notification_service.dto.AdminSendNotificationResponseDTO;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notify")
public class NotificationController {

    private final AdminNotificationService service;
    private final TenantContext tenantContext;

    @PostMapping("/send")
    public AdminSendNotificationResponseDTO sendNotificationToResidents(
            @RequestBody AdminSendNotificationRequestDTO dto
    ) {
        String tenantId = tenantContext.getTenantId();
        String senderUserId = tenantContext.getUserId();

        return service.sendImmediateNotification(tenantId, senderUserId, dto);
    }    
}
