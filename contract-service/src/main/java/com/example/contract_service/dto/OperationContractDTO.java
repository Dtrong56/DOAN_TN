package com.example.contract_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OperationContractDTO {
    private String contractId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal pricePerM2;
    private String description;
}
