package com.example.contract_service.repository;

import com.example.contract_service.entity.ServiceAppendix;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ServiceAppendixRepository extends JpaRepository<ServiceAppendix, UUID> {
    List<ServiceAppendix> findByMainContract_Id(UUID contractId);
}
