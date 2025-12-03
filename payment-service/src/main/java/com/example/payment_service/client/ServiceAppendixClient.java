package com.example.payment_service.client;

import com.example.payment_service.dto.ServiceAppendixDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "contract-service",
        path = "/api/internal/service-appendices"
)
public interface ServiceAppendixClient {

    @GetMapping("/active")
    List<ServiceAppendixDTO> getActiveAppendices(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("residentId") String residentId,
            @RequestParam("periodMonth") int periodMonth,
            @RequestParam("periodYear") int periodYear
    );
}
