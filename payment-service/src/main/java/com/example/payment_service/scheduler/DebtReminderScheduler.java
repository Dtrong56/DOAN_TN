package com.example.payment_service.scheduler;

import com.example.payment_service.client.MonitoringClient;
import com.example.payment_service.client.NotificationClient;
import com.example.payment_service.dto.SystemLogDTO;
import com.example.payment_service.entity.DebtReminderJob;
import com.example.payment_service.entity.Invoice;
import com.example.payment_service.repository.DebtReminderJobRepository;
import com.example.payment_service.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebtReminderScheduler {

    private final InvoiceRepository invoiceRepository;
    private final DebtReminderJobRepository jobRepository;
    private final NotificationClient notificationClient;
    private final MonitoringClient monitoringClient;

    /**
     * Escalation mốc (số ngày so với dueDate)
     *  - trước hạn: 5, 3, 1
     *  - sau hạn (quá hạn): 1, 3, 7
     *
     * Chạy hàng ngày lúc 08:00
     */
    private static final int[] PRE_DUE_DAYS = {5, 3, 1};
    private static final int[] POST_DUE_DAYS = {1, 3, 7};

    @Scheduled(cron = "0 0 8 * * ?")
    public void runDailyDebtReminder() {
        LocalDate today = LocalDate.now();
        log.info("DebtReminderScheduler start for date {}", today);

        // Query all unpaid invoices (có thể tối ưu phân trang nếu số lượng lớn)
        List<Invoice> unpaid = invoiceRepository.findByStatus(Invoice.Status.UNPAID);

        int sentCount = 0;
        int skippedCount = 0;

        for (Invoice invoice : unpaid) {

            LocalDate dueDate = invoice.getDueDate();
            if (dueDate == null) continue;

            long daysLeft = ChronoUnit.DAYS.between(today, dueDate); // positive => before due; negative => overdue

            String reminderType = null;
            if (daysLeft >= 0) {
                // before due
                for (int d : PRE_DUE_DAYS) {
                    if (daysLeft == d) {
                        reminderType = "DUE_IN_" + d; // e.g. DUE_IN_5
                        break;
                    }
                }
            } else {
                // overdue
                long overdueDays = -daysLeft;
                for (int d : POST_DUE_DAYS) {
                    if (overdueDays == d) {
                        reminderType = "OVERDUE_" + d; // e.g. OVERDUE_3
                        break;
                    }
                }
            }

            if (reminderType == null) {
                // not a configured reminder day
                continue;
            }

            // Avoid duplicate reminder for the same invoice in the same day
            LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

            boolean alreadySent = jobRepository.existsByInvoiceIdAndScheduledAtBetween(
                    invoice.getId(), startOfDay, endOfDay
            );

            if (alreadySent) {
                skippedCount++;
                continue;
            }

            // build message
            String message = buildMessage(invoice, daysLeft, reminderType);

            // create job (RUNNING -> update to DONE/FAILED)
            DebtReminderJob job = new DebtReminderJob();
            job.setTenantId(invoice.getTenantId());
            job.setInvoiceId(invoice.getId());
            job.setScheduledAt(LocalDateTime.now());
            job.setStatus(DebtReminderJob.Status.RUNNING);
            job.setRetryCount(0);

            jobRepository.save(job); // persist RUNNING row

            try {
                // send notification (sync feign call)
                notificationClient.sendNotification(
                        invoice.getTenantId(),
                        invoice.getResidentId(),
                        reminderType,
                        message
                );

                // mark job done
                job.setStatus(DebtReminderJob.Status.DONE);
                job.setExecutedAt(LocalDateTime.now());
                jobRepository.save(job);

                // monitoring log
                monitoringClient.createLog(SystemLogDTO.builder()
                        .timestamp(LocalDateTime.now())
                        .tenantId(invoice.getTenantId())
                        .userId("SYSTEM")
                        .role("SYSTEM")
                        .action("DEBT_REMINDER")
                        .objectType("Invoice")
                        .objectId(invoice.getId())
                        .message("Sent debt reminder: " + reminderType)
                        .serviceName("payment-service")
                        .endpoint("DebtReminderScheduler")
                        .build());

                sentCount++;

            } catch (Exception ex) {
                // failed -> update job
                job.setStatus(DebtReminderJob.Status.FAILED);
                job.setExecutedAt(LocalDateTime.now());
                job.setErrorMessage(ex.getMessage());
                job.setRetryCount(job.getRetryCount() + 1);
                jobRepository.save(job);

                log.error("Failed to send debt reminder for invoice {}: {}", invoice.getId(), ex.getMessage());

                // monitoring for failure
                try {
                    monitoringClient.createLog(SystemLogDTO.builder()
                            .timestamp(LocalDateTime.now())
                            .tenantId(invoice.getTenantId())
                            .userId("SYSTEM")
                            .role("SYSTEM")
                            .action("DEBT_REMINDER_FAILED")
                            .objectType("Invoice")
                            .objectId(invoice.getId())
                            .message("Debt reminder failed: " + ex.getMessage())
                            .serviceName("payment-service")
                            .endpoint("DebtReminderScheduler")
                            .build());
                } catch (Exception ignore) {
                    log.warn("Monitoring log failed too for invoice {}", invoice.getId());
                }
            }
        }

        log.info("DebtReminderScheduler finished. Sent: {}, Skipped (duplicate today): {}, Total unpaid scanned: {}",
                sentCount, skippedCount, unpaid.size());
    }

    private String buildMessage(Invoice invoice, long daysLeft, String reminderType) {
        if (reminderType.startsWith("DUE_IN_")) {
            int days = Integer.parseInt(reminderType.substring("DUE_IN_".length()));
            return String.format("Hóa đơn %s của bạn sẽ đến hạn sau %d ngày (hạn: %s). Vui lòng thanh toán.",
                    invoice.getId(), days, invoice.getDueDate().toString());
        } else if (reminderType.startsWith("OVERDUE_")) {
            int days = Integer.parseInt(reminderType.substring("OVERDUE_".length()));
            return String.format("Hóa đơn %s đã quá hạn %d ngày (hạn: %s). Vui lòng thanh toán ngay để tránh bị phạt.",
                    invoice.getId(), days, invoice.getDueDate().toString());
        } else {
            return "Nhắc nợ cho hóa đơn " + invoice.getId();
        }
    }
}
