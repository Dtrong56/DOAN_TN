package com.example.payment_service.repository;

import com.example.payment_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;


public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    List<Invoice> findByTenantIdAndPeriodYearAndPeriodMonth(String tenantId, int year, int month);
    // New:
    List<Invoice> findByTenantIdAndResidentIdAndPeriodYearAndPeriodMonth(String tenantId, String residentId, int year, int month);

    List<Invoice> findByTenantIdAndResidentIdAndPeriodYearAndPeriodMonthAndStatus(String tenantId, String residentId, int year, int month, Invoice.Status status);

    Optional<Invoice> findByIdAndTenantIdAndResidentId(String id, String tenantId, String residentId);

    @Query("""
        SELECT COALESCE(SUM(i.totalAmount),0)
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.periodYear = :year
          AND i.periodMonth = :month
    """)
    BigDecimal sumInvoiceAmountForPeriod(@Param("tenantId") String tenantId,
                                         @Param("year") int year,
                                         @Param("month") int month);

    @Query("""
        SELECT COALESCE(SUM(i.totalAmount),0)
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.periodYear >= :fromYear AND (i.periodYear < :toYear OR (i.periodYear = :toYear AND i.periodMonth <= :toMonth))
          AND (i.periodYear > :fromYear OR (i.periodYear = :fromYear AND i.periodMonth >= :fromMonth))
    """)
    BigDecimal sumInvoiceAmountBetweenMonths(@Param("tenantId") String tenantId,
                                             @Param("fromYear") int fromYear,
                                             @Param("fromMonth") int fromMonth,
                                             @Param("toYear") int toYear,
                                             @Param("toMonth") int toMonth);

    @Query("""
        SELECT COALESCE(SUM(i.totalAmount),0)
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.status IN :statuses
          AND i.periodYear <= :toYear
          AND (i.periodYear < :toYear OR i.periodMonth <= :toMonth)
    """)
    BigDecimal sumOutstandingUpTo(@Param("tenantId") String tenantId,
                                  @Param("statuses") java.util.List<Invoice.Status> statuses,
                                  @Param("toYear") int toYear,
                                  @Param("toMonth") int toMonth);

    // For monthly debt grouping
    @Query("""
        SELECT i.periodYear as y, i.periodMonth as m, COALESCE(SUM(i.totalAmount),0) as s
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND (i.status = 'UNPAID' OR i.status = 'OVERDUE')
          AND i.periodYear >= :fromYear AND (i.periodYear < :toYear OR (i.periodYear = :toYear AND i.periodMonth <= :toMonth))
          AND (i.periodYear > :fromYear OR (i.periodYear = :fromYear AND i.periodMonth >= :fromMonth))
        GROUP BY i.periodYear, i.periodMonth
        ORDER BY i.periodYear, i.periodMonth
    """)
    List<Object[]> sumDebtGroupByMonth(@Param("tenantId") String tenantId,
                                       @Param("fromYear") int fromYear,
                                       @Param("fromMonth") int fromMonth,
                                       @Param("toYear") int toYear,
                                       @Param("toMonth") int toMonth);
}

