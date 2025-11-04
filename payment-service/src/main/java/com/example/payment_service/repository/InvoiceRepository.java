package com.example.payment_service.repository;

import com.example.payment_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    List<Invoice> findByTenantIdAndPeriodYearAndPeriodMonth(String tenantId, int year, int month);
}

