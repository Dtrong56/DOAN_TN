package com.example.contract_service.client;

import com.example.contract_service.dto.NotificationEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/notify/internal")
public interface NotificationClient {

    @PostMapping("/event")
    void sendEvent(@RequestBody NotificationEventDTO dto);
}

