package com.example.monitoring_service.repository;

import com.example.monitoring_service.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface SystemLogRepository extends JpaRepository<SystemLog, String> {

    List<SystemLog> findByTenantIdOrderByTimestampDesc(String tenantId);

    List<SystemLog> findByUserIdOrderByTimestampDesc(String userId);

    // Lấy theo timestamp giảm dần, giới hạn bằng Pageable (sử dụng PageRequest.of(0, limit))
    List<SystemLog> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT l FROM SystemLog l WHERE l.action LIKE %:keyword% OR l.description LIKE %:keyword% ORDER BY l.timestamp DESC")
    List<SystemLog> searchByKeyword(String keyword);
}

