package com.example.payment_service.controller;

import com.example.payment_service.service.InvoiceGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/invoices")
@RequiredArgsConstructor
public class InternalInvoiceController {

    private final InvoiceGenerationService invoiceGenerationService;

    @PostMapping("/generate")
    public String generateInvoices(
            @RequestParam String tenantId,
            @RequestParam int month,
            @RequestParam int year
    ) {
        int count = invoiceGenerationService.generateMonthlyInvoices(tenantId, month, year);
        return "Generated " + count + " invoices for " + month + "/" + year;
    }
}

