package com.example.monitoring_service.controller;

import com.example.monitoring_service.dto.SystemLogDTO;
import com.example.monitoring_service.service.MonitoringInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogMessageListener {

    private final MonitoringInternalService monitoringInternalService;

    @RabbitListener(queues = "system.log.queue")
    public void receiveLog(SystemLogDTO dto) {
        monitoringInternalService.saveLog(dto);
    }
}