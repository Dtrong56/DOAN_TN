package com.example.resident_service.repository;

import com.example.resident_service.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartmentRepository extends JpaRepository<Apartment, String> {
    List<Apartment> findByBuildingId(String buildingId);
}
