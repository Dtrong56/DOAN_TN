package com.example.payment_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class RevenueDebtReportDTO {
    private ReportSummaryDTO summary;
    private List<MonthlyReportDTO> monthly;
}
