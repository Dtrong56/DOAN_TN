package com.example.service_catalog_service.repository;

import com.example.service_catalog_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, String> {
    List<RegistrationRequest> findByTenantIdAndStatus(String tenantId, RegistrationRequest.Status status);
}
