package com.example.monitoring_service.controller;

import com.example.monitoring_service.dto.SystemLogDTO;
import com.example.monitoring_service.service.MonitoringInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/monitoring/internal")
@RequiredArgsConstructor
public class MonitoringInternalController {

    private final MonitoringInternalService monitoringInternalService;

    @PostMapping("/log")
    public void createLog(@RequestBody SystemLogDTO dto) {
        monitoringInternalService.saveLog(dto);
    }
}
//