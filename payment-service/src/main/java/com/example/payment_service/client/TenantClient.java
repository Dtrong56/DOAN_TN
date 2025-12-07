package com.example.payment_service.client;

import com.example.payment_service.dto.TenantDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "tenant-service", path = "/api/internal/tenants")
public interface TenantClient {

    @GetMapping("/active")
    List<TenantDTO> getActiveTenants();
}
