package com.example.monitoring_service.service;

import com.example.monitoring_service.dto.SystemLogDTO;
import com.example.monitoring_service.entity.SystemLog;
import com.example.monitoring_service.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonitoringInternalService {

    private final SystemLogRepository systemLogRepository;

    public void saveLog(SystemLogDTO dto) {

        SystemLog log = new SystemLog(
                dto.getAction(),
                dto.getUserId(),
                dto.getTenantId(),
                dto.getObjectType(),
                dto.getObjectId(),
                dto.getMessage() // map sang description
        );

        // Nếu cần thêm metadata / traceId / endpoint -> chuyển vào description dạng JSON
        // hoặc thêm cột mới tại entity sau này

        systemLogRepository.save(log);
    }
}
