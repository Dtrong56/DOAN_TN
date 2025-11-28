package com.example.contract_service.repository;

import com.example.contract_service.entity.ServiceAppendix;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.time.LocalDate;

public interface ServiceAppendixRepository extends JpaRepository<ServiceAppendix, String> {
    List<ServiceAppendix> findByMainContract_Id(String contractId);

    List<ServiceAppendix> findByMainContractIdAndResidentId(String mainContractId, String residentId);

     List<ServiceAppendix> findByExpirationDateBeforeAndAppendixStatus(
            LocalDate date,
            String status
    );
}
