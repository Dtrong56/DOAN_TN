package com.example.service_catalog_service.repository;

import com.example.service_catalog_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicePackageRepository extends JpaRepository<ServicePackage, String> {
    List<ServicePackage> findByServiceCatalogId(String serviceCatalogId);
}