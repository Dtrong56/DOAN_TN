package com.example.notification_service.controller;

import com.example.notification_service.entity.NotificationLog;
import com.example.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationLogRepository logRepo;

    /**
     * Chạy lúc 06:00 mỗi ngày
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void sendPendingNotifications() {
        List<NotificationLog> pendingLogs = logRepo.findAll()
                .stream()
                .filter(log -> log.getStatus() == NotificationLog.Status.PENDING)
                .toList();

        if (pendingLogs.isEmpty()) {
            log.info("No pending notifications to send.");
            return;
        }

        for (NotificationLog logEntry : pendingLogs) {
            try {
                // TODO: gọi email service hoặc SMTP để gửi thực tế
                logEntry.setStatus(NotificationLog.Status.SENT);
                logEntry.setSentAt(LocalDateTime.now());
                logRepo.save(logEntry);

                log.info("Sent notification {} to user {}", 
                         logEntry.getNotification().getId(), logEntry.getRecipientUserId());
            } catch (Exception ex) {
                logEntry.setStatus(NotificationLog.Status.FAILED);
                logEntry.setErrorMessage(ex.getMessage());
                logRepo.save(logEntry);

                log.error("Failed to send notification {} to user {}", 
                          logEntry.getNotification().getId(), logEntry.getRecipientUserId(), ex);
            }
        }

        log.info("Processed {} pending notifications.", pendingLogs.size());
    }
}