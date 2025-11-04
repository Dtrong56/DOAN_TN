package com.example.auth_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.UUID;

@Component
public class TenantClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TenantClient(RestTemplate restTemplate, 
                        @Value("${services.tenant.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public String getTenantIdByManager(String userId) {
        String url = baseUrl + "/api/internal/management-accounts/by-user/" + userId;
        Map response = restTemplate.getForObject(url, Map.class);
        if (response != null && response.get("tenantId") != null) {
            return response.get("tenantId").toString();
        }
        return null;
    }
}
