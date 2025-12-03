package com.example.contract_service.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PackageInfoDTO {
    private String id;
    private String serviceId;
    private String name;
    private boolean active;
    private Integer durationMonths; 
    private BigDecimal price;
}


