package com.example.payment_service.dto;

import lombok.Data;

@Data
public class ReportRequest {
    // Either specify month/year ranges, e.g. fromMonth=1, fromYear=2025, toMonth=3, toYear=2025
    // or let frontend pass null to default to current month
    private Integer fromMonth;
    private Integer fromYear;
    private Integer toMonth;
    private Integer toYear;

    // optional: aggregate by MONTH, QUARTER, YEAR
    private String periodType; // "MONTH" | "QUARTER" | "YEAR" (default MONTH)
}
