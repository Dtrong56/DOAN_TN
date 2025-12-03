package com.example.payment_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ServiceAppendixDTO {
    private String appendixId;
    private String serviceId;
    private String packageId;
    private String serviceName;
    private String packageName;
    private BigDecimal unitPrice;
    private LocalDate startDate;
    private LocalDate endDate;
}
