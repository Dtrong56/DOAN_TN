package com.example.contract_service.repository;

import com.example.contract_service.entity.AppendixHistory;
import com.example.contract_service.entity.ServiceAppendix;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppendixHistoryRepository extends JpaRepository<AppendixHistory, String> {
    List<AppendixHistory> findByServiceAppendix_IdOrderByVersionNoDesc(String appendixId);

    Optional<AppendixHistory> findTopByServiceAppendixOrderByVersionNoDesc(ServiceAppendix appendix);

    Optional<AppendixHistory> findTopByServiceAppendixAndChangeTypeOrderByChangedAtDesc(ServiceAppendix appendix, String changeType);

    Optional<AppendixHistory> findPendingExtensionByServiceAppendix_Id(String appendixId);

    long countByServiceAppendix_Id(String appendixId);
}