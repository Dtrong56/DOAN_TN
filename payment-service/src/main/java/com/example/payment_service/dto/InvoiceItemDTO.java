package com.example.payment_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemDTO {
    private String description;
    private BigDecimal amount;
    private String serviceAppendixId;
}
