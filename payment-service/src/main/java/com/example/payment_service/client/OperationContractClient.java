package com.example.payment_service.client;

import com.example.payment_service.dto.OperationContractDTO;
import com.example.payment_service.dto.ServiceAppendixDTO;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "contract-service",
        path = "/api/internal"
)
public interface OperationContractClient {

    @GetMapping("/operation-contract/active")
    OperationContractDTO getActiveContract(@RequestParam("tenantId") String tenantId);

    @GetMapping("/service-appendices/active")
    List<ServiceAppendixDTO> getActiveAppendices(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("residentId") String residentId,
            @RequestParam("periodMonth") int periodMonth,
            @RequestParam("periodYear") int periodYear
    );
}
