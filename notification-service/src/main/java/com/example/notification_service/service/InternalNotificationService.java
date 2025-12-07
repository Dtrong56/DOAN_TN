package com.example.notification_service.service;

import com.example.notification_service.dto.NotificationRequestDTO;
import com.example.notification_service.dto.NotificationResponseDTO;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.entity.NotificationChannel;
import com.example.notification_service.entity.NotificationLog;
import com.example.notification_service.repository.NotificationChannelRepository;
import com.example.notification_service.repository.NotificationLogRepository;
import com.example.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalNotificationService {

    private final NotificationRepository notificationRepo;
    private final NotificationChannelRepository channelRepo;
    private final NotificationLogRepository logRepo;

    private final com.example.notification_service.client.ResidentClient residentClient;
    private final com.example.notification_service.client.AuthClient authClient;

    // Tạo notification và log tương ứng ở trạng thái PENDING
    public String createNotification(NotificationRequestDTO dto) {
        try {
            // 1️⃣ Lấy channel phù hợp theo tenant + channelCode
            NotificationChannel channel = channelRepo
                    .findByTenantIdAndActiveTrue(dto.getTenantId())
                    .stream()
                    .filter(ch -> ch.getChannelCode().equalsIgnoreCase(dto.getChannelType()))
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException("Channel not found for code=" + dto.getChannelType())
                    );

            // 2️⃣ Tạo Notification
            Notification noti = new Notification();
            noti.setTenantId(dto.getTenantId());
            noti.setTitle(dto.getTitle());
            noti.setContent(dto.getMessage());
            noti.setType(dto.getType());
            noti.setChannel(channel);
            noti.setCreatedByUserId(dto.getUserId());
            notificationRepo.save(noti);

            // 3️⃣ Tạo NotificationLog ở trạng thái PENDING
            NotificationLog logEntry = new NotificationLog();
            logEntry.setNotification(noti);
            logEntry.setRecipientUserId(dto.getUserId());
            logEntry.setChannel(channel);
            logEntry.setStatus(NotificationLog.Status.PENDING);
            logEntry.setErrorMessage(null);
            logRepo.save(logEntry);

            log.info(
                "Created notification {} for tenant {} → assigned to user {} (pending)",
                noti.getId(),
                dto.getTenantId(),
                dto.getUserId()
            );

            return noti.getId();

        } catch (Exception e) {
            log.error("Create notification failed for tenant {}: {}", dto.getTenantId(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public NotificationResponseDTO handleInternalSend(
            String tenantId,
            String residentId,
            String type,
            String message
    ) {

        // B1. Lấy userId từ resident-service
        String userId = residentClient.getUserIdByResident(tenantId, residentId);

        // B2. Lấy email từ auth-service
        String email = authClient.getUserEmail(userId);

        // B3. Lấy channel EMAIL theo tenant
        NotificationChannel channel = channelRepo
                .findByTenantIdAndActiveTrue(tenantId)
                .stream()
                .filter(ch -> ch.getChannelCode().equalsIgnoreCase("EMAIL"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No EMAIL channel"));

        // B4. Tạo Notification
        Notification noti = new Notification();
        noti.setTenantId(tenantId);
        noti.setTitle("Thông báo tự động");
        noti.setContent(message);
        noti.setType(type);
        noti.setChannel(channel);
        noti.setCreatedByUserId("SYSTEM");
        notificationRepo.save(noti);

        // B5. Tạo NotificationLog
        NotificationLog logEntry = new NotificationLog();
        logEntry.setNotification(noti);
        logEntry.setRecipientUserId(userId);
        logEntry.setChannel(channel);
        logEntry.setStatus(NotificationLog.Status.PENDING);
        logRepo.save(logEntry);

        // Reply
        return NotificationResponseDTO.builder()
        .notificationId(noti.getId())
        .logId(logEntry.getId())
        .status("QUEUED")
        .email(email)
        .message("Notification queued successfully")
        .timestamp(LocalDateTime.now())
        .build();

    }
}
