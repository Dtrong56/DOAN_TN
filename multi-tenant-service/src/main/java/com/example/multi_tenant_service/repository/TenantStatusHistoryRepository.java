package com.example.multi_tenant_service.repository;

import com.example.multi_tenant_service.entity.TenantStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantStatusHistoryRepository extends JpaRepository<TenantStatusHistory, String> {
    List<TenantStatusHistory> findByTenantIdOrderByChangedAtDesc(String tenantId);
}
