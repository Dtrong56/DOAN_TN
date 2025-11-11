package com.example.contract_service.repository;

import com.example.contract_service.entity.AppendixHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppendixHistoryRepository extends JpaRepository<AppendixHistory, String> {
    List<AppendixHistory> findByServiceAppendix_IdOrderByVersionNoDesc(String appendixId);
}
