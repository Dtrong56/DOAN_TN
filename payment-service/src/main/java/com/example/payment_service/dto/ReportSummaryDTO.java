package com.example.payment_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportSummaryDTO {
    private BigDecimal totalRevenue = BigDecimal.ZERO; // sum of successful online + offline
    private BigDecimal totalOffline = BigDecimal.ZERO; // payment_record
    private BigDecimal totalOnline = BigDecimal.ZERO;  // payment_transaction success
    private BigDecimal totalDebt = BigDecimal.ZERO;    // sum of unpaid invoices at end of period
}
