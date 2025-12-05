package com.example.payment_service.dto;

import com.example.payment_service.entity.PaymentRecord;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DirectPaymentRequest {
    private String invoiceId;
    private BigDecimal amount;
    private PaymentRecord.Method method; // CASH, BANK_TRANSFER
    private String note;
}
