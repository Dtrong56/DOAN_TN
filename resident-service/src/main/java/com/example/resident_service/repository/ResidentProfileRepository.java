package com.example.resident_service.repository;

import com.example.resident_service.entity.ResidentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResidentProfileRepository extends JpaRepository<ResidentProfile, UUID> {
    Optional<ResidentProfile> findByUserId(UUID userId);
    Optional<ResidentProfile> findByCccd(String cccd);
}
