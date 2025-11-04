package com.example.payment_service.repository;

import com.example.payment_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByInvoiceId(String invoiceId);
}
