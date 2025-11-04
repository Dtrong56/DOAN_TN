package com.example.contract_service.repository;

import com.example.contract_service.entity.SignatureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, UUID> {
    List<SignatureRecord> findByServiceAppendix_Id(UUID appendixId);
}