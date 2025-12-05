package com.example.payment_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceDetailDTO {
    private String invoiceId;
    private String apartmentId;
    private Integer periodMonth;
    private Integer periodYear;
    private BigDecimal totalAmount;
    private String status;
    private LocalDate dueDate;

    private List<InvoiceItemDTO> items;
    private BigDecimal itemsTotal;
}
