package com.example.auth_service.repository;

import com.example.auth_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, String> {
    Optional<Credential> findByUserId(String userId);
    
}
