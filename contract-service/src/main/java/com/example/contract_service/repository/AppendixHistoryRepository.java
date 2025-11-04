package com.example.contract_service.repository;

import com.example.contract_service.entity.AppendixHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AppendixHistoryRepository extends JpaRepository<AppendixHistory, UUID> {
    List<AppendixHistory> findByServiceAppendix_IdOrderByVersionNoDesc(UUID appendixId);
}
