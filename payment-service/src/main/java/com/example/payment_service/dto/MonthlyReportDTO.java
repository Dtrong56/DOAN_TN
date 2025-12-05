package com.example.payment_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyReportDTO {
    private Integer month;
    private Integer year;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal offline = BigDecimal.ZERO;
    private BigDecimal online = BigDecimal.ZERO;
    private BigDecimal debt = BigDecimal.ZERO;
}
