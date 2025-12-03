package com.example.contract_service.controller;

import com.example.contract_service.dto.OperationContractDTO;
import com.example.contract_service.repository.MainContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/internal/operation-contract")
@RequiredArgsConstructor
public class InternalOperationContractController {

    private final MainContractRepository mainContractRepository;

    @GetMapping("/active")
    public OperationContractDTO getActiveContract(@RequestParam String tenantId) {

        var contract = mainContractRepository
                .findActiveContract(tenantId, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No active operation contract found"));

        OperationContractDTO dto = new OperationContractDTO();
        dto.setContractId(contract.getId());
        dto.setStartDate(contract.getEffectiveDate());
        dto.setEndDate(contract.getExpirationDate());
        dto.setPricePerM2(BigDecimal.valueOf(contract.getPricePerM2()));
        dto.setDescription(contract.getContractCode());

        return dto;
    }
}
