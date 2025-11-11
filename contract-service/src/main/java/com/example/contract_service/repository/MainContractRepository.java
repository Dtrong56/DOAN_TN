package com.example.contract_service.repository;

import com.example.contract_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MainContractRepository extends JpaRepository<MainContract, String> {
    List<MainContract> findByTenantId(String tenantId);
    boolean existsByContractCode(String code);
}




