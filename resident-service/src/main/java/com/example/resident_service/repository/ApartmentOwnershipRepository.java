package com.example.resident_service.repository;

import com.example.resident_service.entity.ApartmentOwnership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApartmentOwnershipRepository extends JpaRepository<ApartmentOwnership, UUID> {
    List<ApartmentOwnership> findByApartmentId(UUID apartmentId);
    List<ApartmentOwnership> findByResidentId(UUID residentId);
}
