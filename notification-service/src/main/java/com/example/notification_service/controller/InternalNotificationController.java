package com.example.notification_service.controller;

import com.example.notification_service.dto.InternalNotificationResponseDTO;
import com.example.notification_service.dto.NotificationRequestDTO;
import com.example.notification_service.service.InternalNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notify/internal")
public class InternalNotificationController {

    private final InternalNotificationService service;

    @PostMapping("/notify")
    public ResponseEntity<InternalNotificationResponseDTO> createNotification(
            @RequestBody NotificationRequestDTO dto
    ) {
        try {
            String notificationId = service.createNotification(dto);

            return ResponseEntity.ok(
                    InternalNotificationResponseDTO.builder()
                            .success(true)
                            .notificationId(notificationId)
                            .message("Notification created successfully")
                            .build()
            );

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    InternalNotificationResponseDTO.builder()
                            .success(false)
                            .notificationId(null)
                            .message("Failed: " + ex.getMessage())
                            .build()
            );
        }
    }
}
