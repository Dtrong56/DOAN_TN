package com.example.contract_service.repository;

import com.example.contract_service.entity.SignatureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, String> {
    List<SignatureRecord> findByServiceAppendix_Id(String appendixId);
}