package com.example.payment_service.controller;

import com.example.payment_service.dto.InvoiceDetailDTO;
import com.example.payment_service.dto.InvoiceSummaryDTO;
import com.example.payment_service.security.TenantContext;
import com.example.payment_service.service.InvoiceResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resident")
@RequiredArgsConstructor
public class InvoiceResidentController {

    private final InvoiceResidentService invoiceResidentService;
    private final TenantContext tenantContext;

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceSummaryDTO>> listInvoices(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "status", required = false) String status
    ) {

        String tenantId = tenantContext.getTenantId();
        String residentId = tenantContext.getResidentId();

        List<InvoiceSummaryDTO> result =
                invoiceResidentService.getInvoices(tenantId, residentId, month, year, status);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<InvoiceDetailDTO> getInvoiceDetail(
            @PathVariable String invoiceId
    ) {
        String tenantId = tenantContext.getTenantId();
        String residentId = tenantContext.getResidentId();

        InvoiceDetailDTO dto =
                invoiceResidentService.getInvoiceDetail(tenantId, residentId, invoiceId);

        return ResponseEntity.ok(dto);
    }
}
