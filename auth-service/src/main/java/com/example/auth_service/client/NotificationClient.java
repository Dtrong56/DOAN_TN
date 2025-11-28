package com.example.auth_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import com.example.auth_service.dto.NotificationRequestDTO;

import java.util.Map;

@Component
public class NotificationClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NotificationClient(RestTemplate restTemplate, 
                          @Value("${services.resident.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @PostMapping("/notify")
    void sendNotification(@RequestBody NotificationRequestDTO payload) {
        restTemplate.postForObject(baseUrl + "/notify", payload, Void.class);
    }
}

