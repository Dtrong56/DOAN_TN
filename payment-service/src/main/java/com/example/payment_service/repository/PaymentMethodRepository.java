package com.example.payment_service.repository;

import com.example.payment_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    List<PaymentMethod> findByTenantIdAndActiveTrue(String tenantId);
}
