package com.example.resident_service.repository;

import com.example.resident_service.entity.ResidentAccount;
import com.example.resident_service.entity.ResidentAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResidentAccountRepository extends JpaRepository<ResidentAccount, UUID> {
    List<ResidentAccount> findByTenantId(UUID tenantId);
    Optional<ResidentAccount> findByUserId(UUID userId);
    List<ResidentAccount> findByStatus(ResidentAccountStatus status);
    Optional<ResidentAccount> findFirstByUserId(UUID userId);
}
