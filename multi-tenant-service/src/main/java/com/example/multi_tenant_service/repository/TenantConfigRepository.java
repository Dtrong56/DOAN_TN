package com.example.multi_tenant_service.repository;

import com.example.multi_tenant_service.entity.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantConfigRepository extends JpaRepository<TenantConfig, String> {
    List<TenantConfig> findByTenantId(String tenantId);
    Optional<TenantConfig> findByTenantIdAndConfigKey(String tenantId, String configKey);
}
