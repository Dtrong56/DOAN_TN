package com.example.payment_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCallbackResult {
    private String transactionId;
    private String status;
    private String invoiceStatus;
}

