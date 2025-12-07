package com.example.notification_service.service;

import com.example.notification_service.client.AuthClient;
import com.example.notification_service.client.MonitoringClient;
import com.example.notification_service.dto.AdminSendNotificationRequestDTO;
import com.example.notification_service.dto.AdminSendNotificationResponseDTO;
import com.example.notification_service.dto.SystemLogDTO;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.entity.NotificationChannel;
import com.example.notification_service.entity.NotificationLog;
import com.example.notification_service.repository.NotificationChannelRepository;
import com.example.notification_service.repository.NotificationLogRepository;
import com.example.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private final NotificationRepository notificationRepo;
    private final NotificationLogRepository logRepo;
    private final NotificationChannelRepository channelRepo;

    private final AuthClient authClient;
    private final EmailService emailService;
    private final MonitoringClient monitoringClient; // <-- Feign client

    /**
     * Gửi thông báo ngay lập tức cho danh sách residentIds.
     * tenantId + senderUserId lấy từ controller (TenantContext).
     */
    public AdminSendNotificationResponseDTO sendImmediateNotification(
            String tenantId,
            String senderUserId,
            AdminSendNotificationRequestDTO dto
    ) {

        Map<String, String> results = new HashMap<>();

        // 1. Lấy channel EMAIL cho tenant
        NotificationChannel channel = channelRepo
                .findByTenantIdAndActiveTrue(tenantId)
                .stream()
                .filter(ch -> ch.getChannelCode().equalsIgnoreCase("EMAIL"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Active EMAIL channel not found for tenant " + tenantId));

        // 2. Ghi system log: bắt đầu gửi (tổng quan)
        try {
            SystemLogDTO startLog = SystemLogDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .userId(senderUserId)
                    .tenantId(tenantId)
                    .role("BQL")
                    .action("ADMIN_SEND_NOTIFICATION_START")
                    .objectType("BulkNotification")
                    .objectId(null)
                    .message(String.format("Admin %s starts sending notification to %d residents", senderUserId, dto.getResidentIds().size()))
                    .serviceName("NotificationService")
                    .endpoint("/notify/admin/send")
                    .traceId(null)
                    .metadata(Map.of("title", dto.getTitle()))
                    .build();

            monitoringClient.createLog(startLog);
        } catch (Exception ex) {
            log.warn("Monitoring service unavailable or failed to log start: {}", ex.getMessage());
        }

        // 3. Loop từng cư dân
        for (String residentId : dto.getResidentIds()) {
            Notification noti = null;
            try {
                // 3.1 Lấy email cư dân từ Auth Service
                String email = authClient.getUserEmail(residentId);

                // 3.2 Tạo Notification và lưu
                noti = new Notification();
                noti.setTenantId(tenantId);
                noti.setTitle(dto.getTitle());
                noti.setContent(dto.getMessage());
                noti.setType("ADMIN_SEND");
                noti.setChannel(channel);
                noti.setCreatedByUserId(senderUserId);
                notificationRepo.save(noti);

                // 3.3 Gửi email ngay lập tức
                emailService.sendEmail(email, dto.getTitle(), dto.getMessage());

                // 3.4 Lưu NotificationLog SENT
                NotificationLog logEntry = new NotificationLog();
                logEntry.setNotification(noti);
                logEntry.setRecipientUserId(residentId);
                logEntry.setChannel(channel);
                logEntry.setStatus(NotificationLog.Status.SENT);
                logEntry.setSentAt(LocalDateTime.now());
                logRepo.save(logEntry);

                results.put(residentId, "SENT");

                // 3.5 Ghi system log cho từng recipient (SENT)
                try {
                    SystemLogDTO perLog = SystemLogDTO.builder()
                            .timestamp(LocalDateTime.now())
                            .userId(senderUserId)
                            .tenantId(tenantId)
                            .role("BQL")
                            .action("ADMIN_SEND_NOTIFICATION_TO_RESIDENT")
                            .objectType("Notification")
                            .objectId(noti.getId())
                            .message(String.format("Notification %s SENT to resident %s (email=%s)", noti.getId(), residentId, email))
                            .serviceName("NotificationService")
                            .endpoint("/notify/admin/send")
                            .traceId(null)
                            .metadata(Map.of("residentId", residentId, "status", "SENT"))
                            .build();

                    monitoringClient.createLog(perLog);
                } catch (Exception ex) {
                    log.warn("Monitoring createLog failed for SENT record (resident={}): {}", residentId, ex.getMessage());
                }

            } catch (Exception ex) {
                log.error("Failed to send notification to {}: {}", residentId, ex.getMessage());

                // Lưu log FAILED (liên quan đến notification nếu có)
                try {
                    NotificationLog failedLog = new NotificationLog();
                    if (noti != null) {
                        failedLog.setNotification(noti);
                    }
                    failedLog.setRecipientUserId(residentId);
                    failedLog.setChannel(channel);
                    failedLog.setStatus(NotificationLog.Status.FAILED);
                    failedLog.setErrorMessage(ex.getMessage());
                    failedLog.setSentAt(null);
                    logRepo.save(failedLog);
                } catch (Exception repoEx) {
                    log.error("Failed to persist failed NotificationLog for {}: {}", residentId, repoEx.getMessage());
                }

                results.put(residentId, "FAILED");

                // Ghi system log cho từng recipient (FAILED)
                try {
                    SystemLogDTO perFailLog = SystemLogDTO.builder()
                            .timestamp(LocalDateTime.now())
                            .userId(senderUserId)
                            .tenantId(tenantId)
                            .role("BQL")
                            .action("ADMIN_SEND_NOTIFICATION_TO_RESIDENT_FAILED")
                            .objectType("Notification")
                            .objectId(noti != null ? noti.getId() : null)
                            .message(String.format("Notification FAILED to resident %s: %s", residentId, ex.getMessage()))
                            .serviceName("NotificationService")
                            .endpoint("/notify/admin/send")
                            .traceId(null)
                            .metadata(Map.of("residentId", residentId, "error", ex.getMessage()))
                            .build();

                    monitoringClient.createLog(perFailLog);
                } catch (Exception mEx) {
                    log.warn("Monitoring createLog failed for FAILED record (resident={}): {}", residentId, mEx.getMessage());
                }
            }
        }

        // 4. Ghi system log: kết thúc gửi (tổng quan)
        try {
            SystemLogDTO endLog = SystemLogDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .userId(senderUserId)
                    .tenantId(tenantId)
                    .role("BQL")
                    .action("ADMIN_SEND_NOTIFICATION_END")
                    .objectType("BulkNotification")
                    .objectId(null)
                    .message(String.format("Admin %s finished sending notifications. Results: %s", senderUserId, results.toString()))
                    .serviceName("NotificationService")
                    .endpoint("/notify/admin/send")
                    .traceId(null)
                    .metadata(Map.of("results", results))
                    .build();

            monitoringClient.createLog(endLog);
        } catch (Exception ex) {
            log.warn("Monitoring service unavailable or failed to log end: {}", ex.getMessage());
        }

        return AdminSendNotificationResponseDTO.builder()
                .success(true)
                .message("Processed admin notification request")
                .results(results)
                .build();
    }
}
