package com.example.payment_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceSummaryDTO {
    private String invoiceId;
    private Integer periodMonth;
    private Integer periodYear;
    private BigDecimal totalAmount;
    private String status;
    private LocalDate dueDate;
}
