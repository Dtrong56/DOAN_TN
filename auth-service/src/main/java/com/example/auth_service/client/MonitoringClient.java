package com.example.auth_service.client;

import com.example.auth_service.dto.SystemLogDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;


@Component
public class MonitoringClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public MonitoringClient(RestTemplate restTemplate,
                            @Value("${services.monitoring.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Gửi log sang monitoring-service
     */
    public void createLog(SystemLogDTO logDTO) {
        String url = baseUrl + "/internal/monitoring/logs";
        restTemplate.postForObject(url, logDTO, Void.class);
    }
}

