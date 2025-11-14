package com.example.resident_service.repository;

import com.example.resident_service.entity.ApartmentOwnership;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
//
public interface ApartmentOwnershipRepository extends JpaRepository<ApartmentOwnership, String> {
    List<ApartmentOwnership> findByApartmentId(String apartmentId);
    List<ApartmentOwnership> findByResidentId(String residentId);
    @Query("SELECT ao FROM ApartmentOwnership ao WHERE ao.apartment.id = :apartmentId AND ao.isRepresentative = true")
    Optional<ApartmentOwnership> findActiveOwnership(@Param("apartmentId") String apartmentId);

}
