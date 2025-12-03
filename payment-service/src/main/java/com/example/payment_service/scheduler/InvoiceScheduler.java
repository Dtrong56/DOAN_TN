package com.example.payment_service.scheduler;

import com.example.payment_service.service.InvoiceGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceScheduler {

    private final InvoiceGenerationService invoiceGenerationService;

    // Chạy lúc 00:30 ngày 1 mỗi tháng
    @Scheduled(cron = "0 30 0 1 * ?")
    public void runMonthly() {

        LocalDate now = LocalDate.now();

        int month = now.minusMonths(1).getMonthValue();
        int year = now.minusMonths(1).getYear();

        String tenantId = "ALL"; 
        // Nếu bạn chạy multi-tenant có bảng tenant, bạn loop tenant list ở đây

        log.info("Running monthly invoice scheduler for {}/{}", month, year);

        // Ở đây giả định mỗi scheduler chạy theo tenant-cụ-thể
        // Nếu nhiều tenants, bạn loop list tenants
        int count = invoiceGenerationService.generateMonthlyInvoices(
                tenantId, month, year
        );

        log.info("Scheduler done — created {} invoices", count);
    }
}
