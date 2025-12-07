package com.example.notification_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "resident-service", path = "/api/internal")
public interface ResidentClient {

    @GetMapping("/{tenantId}/{residentId}/user-id")
    String getUserIdByResident(
            @PathVariable String tenantId,  
            @PathVariable String residentId
    );
}

