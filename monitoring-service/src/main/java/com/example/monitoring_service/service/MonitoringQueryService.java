package com.example.monitoring_service.service;

import com.example.monitoring_service.dto.SystemLogResponseDTO;
import com.example.monitoring_service.entity.SystemLog;
import com.example.monitoring_service.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitoringQueryService {

    private final SystemLogRepository repository;

    public List<SystemLogResponseDTO> findRecent(int limit) {
        if (limit <= 0) {
            limit = 50; // default fallback
        }
        // Sử dụng PageRequest để giới hạn số bản ghi và truy vấn theo timestamp DESC trong DB
        List<SystemLog> logs = repository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit));
        return logs.stream().map(this::toDTO).toList();
    }

    public List<SystemLogResponseDTO> findByTenant(String tenantId) {
        return repository.findByTenantIdOrderByTimestampDesc(tenantId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private SystemLogResponseDTO toDTO(SystemLog log) {
        return SystemLogResponseDTO.builder()
                .id(log.getId())
                .timestamp(log.getTimestamp())
                .userId(log.getUserId())
                .tenantId(log.getTenantId())
                .action(log.getAction())
                .objectType(log.getObjectType())
                .objectId(log.getObjectId())
                .description(log.getDescription())
                .build();
    }
}
