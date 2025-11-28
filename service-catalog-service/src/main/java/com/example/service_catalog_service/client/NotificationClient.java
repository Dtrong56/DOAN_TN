package com.example.service_catalog_service.client;

import com.example.service_catalog_service.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/notify/internal")
public interface NotificationClient {

    // @PostMapping("/event")
    // void sendEvent(@RequestBody NotificationEventDTO dto);

    @PostMapping("/notify")
    void sendNotification(@RequestBody NotificationRequestDTO payload);
}

