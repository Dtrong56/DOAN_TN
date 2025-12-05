package com.example.payment_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DirectPaymentResponse {
    private String paymentRecordId;
    private String invoiceId;
    private BigDecimal amount;
    private String method;
    private LocalDateTime paidAt;
    private String processedBy;
}
