package com.example.notification_service.scheduler;

import com.example.notification_service.entity.NotificationLog;
import com.example.notification_service.repository.NotificationLogRepository;
import com.example.notification_service.client.AuthClient;
import com.example.notification_service.service.EmailService;
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
    private final AuthClient authClient;
    private final EmailService emailService;

    /**
     * Chạy lúc 06:00 mỗi ngày
     */
    // @Scheduled(cron = "0 0 6 * * *")
    // @Transactional
    // public void sendPendingNotifications() {

    //     List<NotificationLog> pendingLogs = logRepo.findAll()
    //             .stream()
    //             .filter(log -> log.getStatus() == NotificationLog.Status.PENDING)
    //             .toList();

    //     if (pendingLogs.isEmpty()) {
    //         log.info("No pending notifications to send.");
    //         return;
    //     }

    //     for (NotificationLog logEntry : pendingLogs) {
    //         try {
    //             String userId = logEntry.getRecipientUserId();

    //             // 1️⃣ Lấy email từ auth-service qua Feign
    //             String email = authClient.getUserEmail(userId);

    //             if (email == null || email.isBlank()) {
    //                 throw new RuntimeException("Email not found for userId=" + userId);
    //             }

    //             // 2️⃣ Gửi email bằng EmailService của bạn
    //             emailService.sendEmail(
    //                     email,
    //                     logEntry.getNotification().getTitle(),
    //                     logEntry.getNotification().getContent()
    //             );

    //             // 3️⃣ Cập nhật trạng thái
    //             logEntry.setStatus(NotificationLog.Status.SENT);
    //             logEntry.setSentAt(LocalDateTime.now());
    //             logRepo.save(logEntry);

    //             log.info("Sent notification {} to user {} (email={})",
    //                     logEntry.getNotification().getId(),
    //                     userId,
    //                     email
    //             );

    //         } catch (Exception ex) {
    //             // 4️⃣ Lỗi → FAILED
    //             logEntry.setStatus(NotificationLog.Status.FAILED);
    //             logEntry.setErrorMessage(ex.getMessage());
    //             logRepo.save(logEntry);

    //             log.error("Failed to send notification {} to user {}: {}",
    //                     logEntry.getNotification().getId(),
    //                     logEntry.getRecipientUserId(),
    //                     ex.getMessage()
    //             );
    //         }
    //     }

    //     log.info("Processed {} pending notifications.", pendingLogs.size());
    // }

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void sendPendingNotifications() {
        List<NotificationLog> pendingLogs = logRepo.findByStatus(NotificationLog.Status.PENDING);

        for (NotificationLog logEntry : pendingLogs) {
            try {
                String userId = logEntry.getRecipientUserId();
                String email = authClient.getUserEmail(userId);

                // Gửi mail thật qua SMTP/MailSender
                emailService.sendEmail(
                        email,
                        logEntry.getNotification().getTitle(),
                        logEntry.getNotification().getContent()
                );

                // Update
                logEntry.setStatus(NotificationLog.Status.SENT);
                logEntry.setSentAt(LocalDateTime.now());
                logRepo.save(logEntry);

            } catch (Exception ex) {
                logEntry.setStatus(NotificationLog.Status.FAILED);
                logEntry.setErrorMessage(ex.getMessage());
                logRepo.save(logEntry);
            }
        }
    }

}
