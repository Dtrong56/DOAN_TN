package com.example.resident_service.repository;

import com.example.resident_service.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, String> {
    List<Building> findByTenantId(String tenantId);
}
