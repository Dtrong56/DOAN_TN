package com.example.contract_service.scheduler;

import com.example.contract_service.entity.ServiceAppendix;
import com.example.contract_service.repository.ServiceAppendixRepository;

import jakarta.annotation.PostConstruct;

import com.example.contract_service.client.NotificationClient;
import com.example.contract_service.dto.NotificationRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppendixExpireScheduler {

    private final ServiceAppendixRepository appendixRepo;
    private final NotificationClient notificationClient;

    /**
     * Chạy lúc 00:10 mỗi ngày
     */
    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void autoExpireAppendices() {

        LocalDate today = LocalDate.now();

        List<ServiceAppendix> expiredList =
                appendixRepo.findByExpirationDateBeforeAndAppendixStatus(today, "APPROVED");

        if (expiredList.isEmpty()) {
            log.info("No appendix expired today.");
            return;
        }

        for (ServiceAppendix appendix : expiredList) {

            appendix.setAppendixStatus("EXPIRED");
            appendixRepo.save(appendix);

            log.info("Appendix {} expired at {}", appendix.getId(), today);

            // 🔔 Gửi thông báo sang Notification Service
            try {
                NotificationRequestDTO dto = NotificationRequestDTO.builder()
                    .tenantId(appendix.getMainContract().getTenantId())
                    .userId(appendix.getResidentId())
                    .title("Phụ lục dịch vụ đã hết hạn")
                    .message("Phụ lục dịch vụ " + appendix.getServiceId() + " đã hết hạn vào ngày " + appendix.getExpirationDate())
                    .type("CONTRACT")
                    .objectType("ServiceAppendix")
                    .objectId(appendix.getId())
                    .action("APPENDIX_EXPIRED")
                    .channelType("EMAIL")
                    .metadata(Map.of("serviceId", appendix.getServiceId()))
                    .build();

                notificationClient.sendNotification(dto);

            } catch (Exception ex) {
                log.error("Failed to notify resident {} for appendix {}",
                        appendix.getResidentId(), appendix.getId(), ex);
            }
        }

        log.info("Expired {} appendices today.", expiredList.size());
    }

    /**
     * Chạy ngay khi service vừa khởi động
     */
    @PostConstruct
    public void init() {
        log.info("Running appendix expire check at startup...");
        autoExpireAppendices();
    }
}

