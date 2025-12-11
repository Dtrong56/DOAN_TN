package com.example.payment_service.repository;

import com.example.payment_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.repository.query.Param;


public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByInvoiceId(String invoiceId);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM PaymentTransaction t
        WHERE (t.paymentMethod IS NOT NULL OR t.invoice IS NOT NULL)
          AND t.invoice.tenantId = :tenantId
          AND t.status = 'SUCCESS'
          AND t.transactionDate >= :from
          AND t.transactionDate <= :to
    """)
    BigDecimal sumSuccessAmountByTenantAndPeriod(
            @Param("tenantId") String tenantId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT YEAR(t.transactionDate) as y, MONTH(t.transactionDate) as m, COALESCE(SUM(t.amount),0) as s
        FROM PaymentTransaction t
        WHERE t.invoice.tenantId = :tenantId
          AND t.status = 'SUCCESS'
          AND t.transactionDate >= :from
          AND t.transactionDate <= :to
        GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate)
        ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)
    """)
    List<Object[]> sumSuccessGroupByMonth(
            @Param("tenantId") String tenantId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
