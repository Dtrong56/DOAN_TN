package com.example.payment_service.repository;

import com.example.payment_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, String> {
    List<InvoiceItem> findByInvoiceId(String invoiceId);
}
