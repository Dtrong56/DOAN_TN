package com.example.contract_service.repository;

import com.example.contract_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface MainContractRepository extends JpaRepository<MainContract, String> {
    List<MainContract> findByTenantId(String tenantId);
    Optional<MainContract> findFirstByTenantId(String tenantId);
    boolean existsByContractCode(String code);
    long countByTenantId(String tenantId);

    @Query("SELECT mc FROM MainContract mc JOIN ServiceAppendix sa ON mc.id = sa.mainContract.id "
         + "WHERE sa.residentId = :residentId")
    List<MainContract> findByResidentId(@Param("residentId") String residentId);

    @Query("""
        SELECT m FROM MainContract m
        WHERE m.tenantId = :tenantId
          AND m.effectiveDate <= :today
          AND m.expirationDate >= :today
    """)
    Optional<MainContract> findActiveContract(String tenantId, LocalDate today);
}

