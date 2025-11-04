package com.example.resident_service.repository;

import com.example.resident_service.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    List<Apartment> findByBuildingId(UUID buildingId);
}
