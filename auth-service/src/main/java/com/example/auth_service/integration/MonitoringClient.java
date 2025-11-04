package com.example.auth_service.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MonitoringClient {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://monitoring-service:8087/api/v1/system-logs") // chỉnh port thực tế nếu khác
            .build();

    public void logAction(String action, String objectType, String objectId, String description, String tenantId) {
        try {
            webClient.post()
                    .uri("")
                    .bodyValue(new SystemLogRequest(action, objectType, objectId, description, tenantId))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();
        } catch (Exception e) {
            System.err.println("❌ Failed to send log: " + e.getMessage());
        }
    }

    private record SystemLogRequest(String action, String objectType, String objectId, String description, String tenantId) {}
}
