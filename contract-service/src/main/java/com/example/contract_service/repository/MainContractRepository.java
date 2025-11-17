package com.example.contract_service.repository;

import com.example.contract_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MainContractRepository extends JpaRepository<MainContract, String> {
    List<MainContract> findByTenantId(String tenantId);
    Optional<MainContract> findFirstByTenantId(String tenantId);
    boolean existsByContractCode(String code);
    long countByTenantId(String tenantId);
}

