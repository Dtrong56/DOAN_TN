package com.example.service_catalog_service.dto;

import java.math.BigDecimal;


public record ServicePackageRequest(
        String name,
        String description,
        Integer durationMonths,
        BigDecimal price
) {}
