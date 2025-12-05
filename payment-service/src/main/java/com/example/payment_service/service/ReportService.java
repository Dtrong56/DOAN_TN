package com.example.payment_service.service;

import com.example.payment_service.dto.MonthlyReportDTO;
import com.example.payment_service.dto.ReportRequest;
import com.example.payment_service.dto.ReportSummaryDTO;
import com.example.payment_service.dto.RevenueDebtReportDTO;
import com.example.payment_service.repository.InvoiceRepository;
import com.example.payment_service.repository.PaymentRecordRepository;
import com.example.payment_service.repository.PaymentTransactionRepository;
import com.example.payment_service.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final TenantContext tenantContext;

    public RevenueDebtReportDTO generateRevenueDebtReport(ReportRequest req) {
        String tenantId = tenantContext.getTenantId();
        if (tenantId == null) throw new RuntimeException("Missing tenant context");

        // default = this month
        LocalDate now = LocalDate.now();
        int fromMonth = (req.getFromMonth() == null) ? now.getMonthValue() : req.getFromMonth();
        int fromYear = (req.getFromYear() == null) ? now.getYear() : req.getFromYear();
        int toMonth = (req.getToMonth() == null) ? now.getMonthValue() : req.getToMonth();
        int toYear = (req.getToYear() == null) ? now.getYear() : req.getToYear();

        // normalize from date = first day of fromMonth
        LocalDate fromDate = LocalDate.of(fromYear, fromMonth, 1);
        LocalDate toDate = LocalDate.of(toYear, toMonth, 1).with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);

        // 1) sums
        BigDecimal offlineSum = paymentRecordRepository.sumAmountByTenantAndPeriod(tenantId, fromDateTime, toDateTime);
        BigDecimal onlineSum = paymentTransactionRepository.sumSuccessAmountByTenantAndPeriod(tenantId, fromDateTime, toDateTime);

        BigDecimal totalRevenue = (offlineSum == null ? BigDecimal.ZERO : offlineSum)
                .add(onlineSum == null ? BigDecimal.ZERO : onlineSum);

        // 2) debt at end of period (sum unpaid/overdue invoices up to toDate)
        List<com.example.payment_service.entity.Invoice.Status> statuses = Arrays.asList(
                com.example.payment_service.entity.Invoice.Status.UNPAID,
                com.example.payment_service.entity.Invoice.Status.OVERDUE
        );

        BigDecimal debt = invoiceRepository.sumOutstandingUpTo(tenantId, statuses, toYear, toMonth);
        if (debt == null) debt = BigDecimal.ZERO;

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.setTotalOffline(offlineSum == null ? BigDecimal.ZERO : offlineSum);
        summary.setTotalOnline(onlineSum == null ? BigDecimal.ZERO : onlineSum);
        summary.setTotalRevenue(totalRevenue);
        summary.setTotalDebt(debt);

        // 3) monthly breakdown
        List<MonthlyReportDTO> monthly = buildMonthlyBreakdown(tenantId, fromDate, toDate);

        RevenueDebtReportDTO out = new RevenueDebtReportDTO();
        out.setSummary(summary);
        out.setMonthly(monthly);
        return out;
    }

    private List<MonthlyReportDTO> buildMonthlyBreakdown(String tenantId, LocalDate fromDate, LocalDate toDate) {
        LocalDate cur = fromDate.withDayOfMonth(1);
        LocalDate end = toDate.withDayOfMonth(1);
        List<MonthlyReportDTO> result = new ArrayList<>();

        // fetch grouped sums from repositories
        List<Object[]> offlineGrouped = paymentRecordRepository.sumAmountGroupByMonth(tenantId, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX));
        List<Object[]> onlineGrouped = paymentTransactionRepository.sumSuccessGroupByMonth(tenantId, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX));
        List<Object[]> debtGrouped = invoiceRepository.sumDebtGroupByMonth(tenantId,
                fromDate.getYear(), fromDate.getMonthValue(),
                toDate.getYear(), toDate.getMonthValue());

        // convert to maps for quick lookup: key = "yyyy-MM"
        Map<String, BigDecimal> offlineMap = new HashMap<>();
        for (Object[] r : offlineGrouped) {
            Integer y = (Integer) r[0];
            Integer m = (Integer) r[1];
            BigDecimal s = (BigDecimal) r[2];
            offlineMap.put(String.format("%04d-%02d", y, m), s == null ? BigDecimal.ZERO : s);
        }
        Map<String, BigDecimal> onlineMap = new HashMap<>();
        for (Object[] r : onlineGrouped) {
            Integer y = (Integer) r[0];
            Integer m = (Integer) r[1];
            BigDecimal s = (BigDecimal) r[2];
            onlineMap.put(String.format("%04d-%02d", y, m), s == null ? BigDecimal.ZERO : s);
        }
        Map<String, BigDecimal> debtMap = new HashMap<>();
        for (Object[] r : debtGrouped) {
            Integer y = (Integer) r[0];
            Integer m = (Integer) r[1];
            BigDecimal s = (BigDecimal) r[2];
            debtMap.put(String.format("%04d-%02d", y, m), s == null ? BigDecimal.ZERO : s);
        }

        while (!cur.isAfter(end)) {
            MonthlyReportDTO m = new MonthlyReportDTO();
            m.setYear(cur.getYear());
            m.setMonth(cur.getMonthValue());
            String key = String.format("%04d-%02d", cur.getYear(), cur.getMonthValue());
            m.setOffline(offlineMap.getOrDefault(key, BigDecimal.ZERO));
            m.setOnline(onlineMap.getOrDefault(key, BigDecimal.ZERO));
            m.setRevenue(m.getOffline().add(m.getOnline()));
            m.setDebt(debtMap.getOrDefault(key, BigDecimal.ZERO));
            result.add(m);
            cur = cur.plusMonths(1);
        }
        return result;
    }
}
