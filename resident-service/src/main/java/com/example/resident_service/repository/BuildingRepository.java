package com.example.resident_service.repository;

import com.example.resident_service.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {
    List<Building> findByTenantId(UUID tenantId);
}
