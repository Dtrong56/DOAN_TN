package com.example.contract_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "monitoring-service")
public interface MonitoringClient {

    @PostMapping("/monitoring/logs")
    void createLog(@RequestBody Map<String, Object> logData);
}
