package com.example.monitoring_service.controller;

import com.example.monitoring_service.dto.SystemLogResponseDTO;
import com.example.monitoring_service.service.MonitoringQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitoring/internal")
@RequiredArgsConstructor
public class MonitoringQueryController {

    private final MonitoringQueryService monitoringQueryService;

    @GetMapping("/log/recent")
    public List<SystemLogResponseDTO> getRecentLogs(
            @RequestParam(defaultValue = "50") int limit) {
        return monitoringQueryService.findRecent(limit);
    }

    @GetMapping("/log/tenant/{tenantId}")
    public List<SystemLogResponseDTO> getLogsByTenant(@PathVariable String tenantId) {
        return monitoringQueryService.findByTenant(tenantId);
    }
}
