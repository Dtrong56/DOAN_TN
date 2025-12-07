package com.example.payment_service.client;

import com.example.payment_service.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", path = "/notify/internal")
public interface NotificationClient {

    // @PostMapping("/event")
    // void sendEvent(@RequestBody NotificationEventDTO dto);

    @PostMapping("/notify")
    void sendNotification(@RequestBody NotificationRequestDTO payload);

    @PostMapping("/send")
    void sendNotification(
        @RequestParam String tenantId,
        @RequestParam String residentId,
        @RequestParam String type,
        @RequestParam String message
    );
}

