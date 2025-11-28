package com.example.notification_service.controller;

import com.example.notification_service.dto.NotificationRequestDTO;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.entity.NotificationChannel;
import com.example.notification_service.entity.NotificationLog;
import com.example.notification_service.repository.NotificationRepository;
import com.example.notification_service.repository.NotificationChannelRepository;
import com.example.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notify/internal")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationRepository notificationRepo;
    private final NotificationChannelRepository channelRepo;
    private final NotificationLogRepository logRepo;

    @PostMapping("/notify")
    public void createNotification(@RequestBody NotificationRequestDTO dto) {
        try {
            // Tìm channel theo tenant + loại kênh
            NotificationChannel channel = channelRepo
                    .findByTenantIdAndActiveTrue(dto.getTenantId())
                    .stream()
                    .filter(ch -> ch.getChannelCode().equalsIgnoreCase(dto.getChannelType()))
                    .findFirst()
                    .orElse(null);

            Notification notification = new Notification();
            notification.setTenantId(dto.getTenantId());
            notification.setTitle(dto.getTitle());
            notification.setContent(dto.getMessage());
            notification.setType(dto.getType());
            notification.setChannel(channel);
            notification.setCreatedByUserId(dto.getUserId());

            notificationRepo.save(notification);

            NotificationLog log = new NotificationLog();
            log.setNotification(notification);
            log.setRecipientUserId(dto.getUserId());
            log.setChannel(channel);
            log.setStatus(NotificationLog.Status.PENDING);

            logRepo.save(log);

            // log.info("Created notification {} for tenant {}", notification.getId(), dto.getTenantId());
        } catch (Exception ex) {
            log.error("Failed to create notification for tenant {}", dto.getTenantId(), ex);
        }
    }
}