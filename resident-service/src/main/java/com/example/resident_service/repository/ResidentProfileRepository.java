package com.example.resident_service.repository;

import com.example.resident_service.entity.ResidentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResidentProfileRepository extends JpaRepository<ResidentProfile, String> {
    Optional<ResidentProfile> findByCccd(String cccd);
    Optional<ResidentProfile> findByCccdAndTenantId(String cccd, String tenantId);
}
