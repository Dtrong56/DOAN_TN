package com.example.notification_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthClient {
    @GetMapping("/email/{userId}")
    String getUserEmail(@PathVariable("userId") String userId);
}

