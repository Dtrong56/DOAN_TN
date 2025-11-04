package com.example.multi_tenant_service.repository;

import com.example.multi_tenant_service.entity.Tenant;
import com.example.multi_tenant_service.entity.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByName(String name);
    List<Tenant> findByStatus(TenantStatus status);
    boolean existsByName(String name);
}
