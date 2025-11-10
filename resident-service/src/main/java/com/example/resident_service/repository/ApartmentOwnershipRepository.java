package com.example.resident_service.repository;

import com.example.resident_service.entity.ApartmentOwnership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApartmentOwnershipRepository extends JpaRepository<ApartmentOwnership, String> {
    List<ApartmentOwnership> findByApartmentId(String apartmentId);
    List<ApartmentOwnership> findByResidentId(String residentId);
    Optional<ApartmentOwnership> findActiveOwnership(String apartmentId, String tenantId);

}
