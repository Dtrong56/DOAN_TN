package com.example.multi_tenant_service.repository;

import com.example.multi_tenant_service.entity.ManagementAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagementAccountRepository extends JpaRepository<ManagementAccount, String> {
    Optional<ManagementAccount> findByTenantId(String tenantId);
    Optional<ManagementAccount> findByTenantIdAndUserId(String tenantId, String userId);
    Optional<ManagementAccount> findFirstByUserId(String userId);
    // tìm ManagementAccount gốc của tenant (root account)
    Optional<ManagementAccount> findByTenant_Id(String tenantId);
}
