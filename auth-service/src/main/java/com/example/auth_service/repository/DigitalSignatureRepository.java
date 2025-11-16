package com.example.auth_service.repository;

import com.example.auth_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, String> {
    Optional<DigitalSignature> findByUserIdAndActiveTrue(String userId);
    Optional<DigitalSignature> findActiveSignatureByUserId(String userId);
}