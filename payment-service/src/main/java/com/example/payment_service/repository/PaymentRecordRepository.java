package com.example.payment_service.repository;

import com.example.payment_service.entity.PaymentRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) 
        FROM PaymentRecord p 
        WHERE p.tenantId = :tenantId 
          AND p.paymentDate >= :from 
          AND p.paymentDate <= :to
    """)
    BigDecimal sumAmountByTenantAndPeriod(
            @Param("tenantId") String tenantId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT YEAR(p.paymentDate) as y, MONTH(p.paymentDate) as m, COALESCE(SUM(p.amount),0) as s
        FROM PaymentRecord p
        WHERE p.tenantId = :tenantId
          AND p.paymentDate >= :from
          AND p.paymentDate <= :to
        GROUP BY YEAR(p.paymentDate), MONTH(p.paymentDate)
        ORDER BY YEAR(p.paymentDate), MONTH(p.paymentDate)
    """)
    List<Object[]> sumAmountGroupByMonth(
            @Param("tenantId") String tenantId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
