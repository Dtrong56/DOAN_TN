package com.example.monitoring_service.controller;

import com.example.monitoring_service.entity.SystemLog;
import com.example.monitoring_service.repository.SystemLogRepository;
import com.example.monitoring_service.dto.SystemLogDTO; // hoặc copy DTO sang monitoring_service.dto
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/monitor/internal")
@RequiredArgsConstructor
public class InternalMonitoringController {

    private final SystemLogRepository systemLogRepository;

    /**
     * API để các service khác gọi sang ghi log
     */
    @PostMapping("/log")
    public void createLog(@RequestBody SystemLogDTO dto) {
        try {
            SystemLog logEntity = new SystemLog();
            logEntity.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
            logEntity.setUserId(dto.getUserId() != null ? dto.getUserId().toString() : null);
            logEntity.setTenantId(dto.getTenantId() != null ? dto.getTenantId().toString() : null);
            logEntity.setAction(dto.getAction());
            logEntity.setObjectType(dto.getObjectType());
            logEntity.setObjectId(dto.getObjectId() != null ? dto.getObjectId().toString() : null);

            // description: có thể ghép từ message + serviceName + endpoint
            String desc = dto.getMessage();
            if (dto.getServiceName() != null) {
                desc += " | Service: " + dto.getServiceName();
            }
            if (dto.getEndpoint() != null) {
                desc += " | Endpoint: " + dto.getEndpoint();
            }
            logEntity.setDescription(desc);

            systemLogRepository.save(logEntity);

            log.info("Saved system log: action={}, user={}, tenant={}", 
                     dto.getAction(), dto.getUserId(), dto.getTenantId());
        } catch (Exception ex) {
            log.error("Failed to save system log for action {}", dto.getAction(), ex);
            throw ex;
        }
    }
}
