package com.example.multi_tenant_service.client;

import com.example.multi_tenant_service.dto.SystemLogDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client để gọi sang monitoring-service
 */
@FeignClient(name = "monitoring-service", path = "/monitoring/internal")
public interface MonitoringClient {

    /**
     * Gửi log hệ thống sang monitoring-service
     */
    @PostMapping("/log")
    void createLog(@RequestBody SystemLogDTO logDTO);
}
