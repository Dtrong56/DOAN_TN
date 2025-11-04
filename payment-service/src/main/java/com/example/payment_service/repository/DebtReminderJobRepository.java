package com.example.payment_service.repository;

import com.example.payment_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DebtReminderJobRepository extends JpaRepository<DebtReminderJob, String> {
    List<DebtReminderJob> findByTenantIdAndStatus(String tenantId, DebtReminderJob.Status status);
}