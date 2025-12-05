package com.example.payment_service.dto;

import lombok.Data;

@Data
public class InitOnlinePaymentRequest {
    private String invoiceId;
    private String paymentMethodId;
}

