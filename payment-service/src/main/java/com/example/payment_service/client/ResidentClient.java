package com.example.payment_service.client;

import com.example.payment_service.dto.ResidentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "resident-service",
        path = "/api/internal/residents"
)
public interface ResidentClient {

    @GetMapping
    List<ResidentDTO> getResidents(
            @RequestParam("tenantId") String tenantId,
            @RequestParam(value = "activeOnly", defaultValue = "true") boolean activeOnly,
            @RequestParam(value = "includeApartment", defaultValue = "true") boolean includeApartment
    );
}
