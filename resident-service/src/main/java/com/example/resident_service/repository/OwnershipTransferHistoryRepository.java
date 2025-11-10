package com.example.resident_service.repository;

import com.example.resident_service.entity.OwnershipTransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnershipTransferHistoryRepository extends JpaRepository<OwnershipTransferHistory, String> {
    List<OwnershipTransferHistory> findByApartmentId(String apartmentId);
}
