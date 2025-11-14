package com.example.resident_service.repository;

import com.example.resident_service.entity.ResidentAccount;
import com.example.resident_service.entity.ResidentAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResidentAccountRepository extends JpaRepository<ResidentAccount, String> {
    List<ResidentAccount> findByTenantId(String tenantId);
    Optional<ResidentAccount> findByUserId(String userId);
    List<ResidentAccount> findByStatus(ResidentAccountStatus status);
    Optional<ResidentAccount> findFirstByUserId(String userId);
    Optional<ResidentAccount> findByTenantIdAndResidentProfileId(String tenantId, String id);
    Optional<ResidentAccount> findFirstByResidentProfile_CccdAndTenantId(String cccd, String tenantId);
}
