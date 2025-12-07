package com.example.payment_service.scheduler;

import com.example.payment_service.client.TenantClient;
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
    private final TenantClient tenantClient;

    // Chạy lúc 00:30 ngày 1 mỗi tháng
    @Scheduled(cron = "0 30 0 1 * ?")
    public void runMonthly() {

        LocalDate now = LocalDate.now();
        int month = now.minusMonths(1).getMonthValue();
        int year = now.minusMonths(1).getYear();

        log.info("InvoiceScheduler START — generating invoices for {}/{}", month, year);

        var tenants = tenantClient.getActiveTenants();

        if (tenants.isEmpty()) {
            log.warn("No active tenants found — skip scheduler.");
            return;
        }

        tenants.forEach(t -> {
            log.info("Generating invoice for Tenant {} ({})", t.getName(), t.getId());

            int count = invoiceGenerationService.generateMonthlyInvoices(
                    t.getId(),
                    month,
                    year
            );

            log.info("Tenant {} — created {} invoices", t.getId(), count);
        });

        log.info("InvoiceScheduler DONE — All tenants processed");
    }
}
