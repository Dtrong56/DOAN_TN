package com.example.notification_service.service;

import com.example.notification_service.dto.NotificationRequestDTO;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.entity.NotificationChannel;
import com.example.notification_service.entity.NotificationLog;
import com.example.notification_service.repository.NotificationChannelRepository;
import com.example.notification_service.repository.NotificationLogRepository;
import com.example.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalNotificationService {

    private final NotificationRepository notificationRepo;
    private final NotificationChannelRepository channelRepo;
    private final NotificationLogRepository logRepo;

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
}
