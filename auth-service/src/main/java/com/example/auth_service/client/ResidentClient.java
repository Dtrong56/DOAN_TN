package com.example.auth_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class ResidentClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ResidentClient(RestTemplate restTemplate, 
                          @Value("${services.resident.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Map getTenantIdByResident(String userId) {
        String url = baseUrl + "/api/internal/resident-accounts/by-user/" + userId;
        Map response = restTemplate.getForObject(url, Map.class);
        if (response != null && response.get("tenantId") != null && response.get("residentId") != null) {
            return response;
        }
        return null;
    }
}
