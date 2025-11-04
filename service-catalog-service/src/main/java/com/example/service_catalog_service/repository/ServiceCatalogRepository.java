package com.example.service_catalog_service.repository;

import com.example.service_catalog_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, String> {
    List<ServiceCatalog> findByTenantIdAndActiveTrue(String tenantId);
}
