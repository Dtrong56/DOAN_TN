package com.example.resident_service.repository;

import com.example.resident_service.entity.OwnershipTransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OwnershipTransferHistoryRepository extends JpaRepository<OwnershipTransferHistory, UUID> {
    List<OwnershipTransferHistory> findByApartmentId(UUID apartmentId);
}
