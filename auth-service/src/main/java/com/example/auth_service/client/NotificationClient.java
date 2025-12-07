package com.example.auth_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.example.auth_service.dto.NotificationRequestDTO;

@Component
public class NotificationClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NotificationClient(RestTemplate restTemplate, 
                          @Value("${services.notification.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Gửi notification qua notification-service
     */
    public void sendNotification(NotificationRequestDTO request) {
        String url = baseUrl + "/internal/notifications";
        restTemplate.postForObject(url, request, Void.class);
    }
}

