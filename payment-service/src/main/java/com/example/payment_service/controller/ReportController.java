package com.example.payment_service.controller;

import com.example.payment_service.dto.ReportRequest;
import com.example.payment_service.dto.RevenueDebtReportDTO;
import com.example.payment_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/internal/reports/revenue-debt?fromMonth=1&fromYear=2025&toMonth=3&toYear=2025
     */
    @GetMapping("/revenue-debt")
    public RevenueDebtReportDTO revenueDebt(
            @RequestParam(required = false) Integer fromMonth,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toMonth,
            @RequestParam(required = false) Integer toYear
    ) {
        ReportRequest req = new ReportRequest();
        req.setFromMonth(fromMonth);
        req.setFromYear(fromYear);
        req.setToMonth(toMonth);
        req.setToYear(toYear);
        return reportService.generateRevenueDebtReport(req);
    }
}
