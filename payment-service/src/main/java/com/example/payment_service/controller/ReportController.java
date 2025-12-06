package com.example.payment_service.controller;

import com.example.payment_service.dto.ReportRequest;
import com.example.payment_service.dto.RevenueDebtReportDTO;
import com.example.payment_service.service.ReportService;
import com.example.payment_service.service.ReportExportService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

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

    /**
     * GET /api/internal/reports/revenue-debt/export?fromMonth=1&fromYear=2025&toMonth=3&toYear=2025&format=pdf
     */
    @GetMapping("/revenue-debt/export")
    public ResponseEntity<byte[]> exportRevenueDebt(
            @RequestParam(required = false) Integer fromMonth,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toMonth,
            @RequestParam(required = false) Integer toYear,
            @RequestParam(required = false, defaultValue = "pdf") String format
    ) throws Exception {
        ReportRequest req = new ReportRequest();
        req.setFromMonth(fromMonth);
        req.setFromYear(fromYear);
        req.setToMonth(toMonth);
        req.setToYear(toYear);

        RevenueDebtReportDTO report = reportService.generateRevenueDebtReport(req);

        byte[] data;
        String contentType;
        String filename;
        if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
            data = reportExportService.exportToExcel(report);
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            filename = "revenue-debt.xlsx";
        } else {
            data = reportExportService.exportToPdf(report);
            contentType = "application/pdf";
            filename = "revenue-debt.pdf";
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .contentLength(data.length)
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .body(data);
    }

}
