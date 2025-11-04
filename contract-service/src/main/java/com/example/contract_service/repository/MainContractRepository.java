package com.example.contract_service.repository;

import com.example.contract_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MainContractRepository extends JpaRepository<MainContract, UUID> {
    List<MainContract> findByTenantId(UUID tenantId);
    boolean existsByContractCode(String code);
}




