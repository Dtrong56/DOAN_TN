package com.example.payment_service.client;

import com.example.payment_service.dto.OperationContractDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "contract-service",
        path = "/api/internal/operation-contract"
)
public interface OperationContractClient {

    @GetMapping("/active")
    OperationContractDTO getActiveContract(@RequestParam("tenantId") String tenantId);
}
