package com.example.service_catalog_service.dto;

import java.math.BigDecimal;

public record UpdateServicePackageRequest(
        String id, // null → gói mới
        String name,
        String description,
        Integer durationMonths,
        BigDecimal price,
        Boolean active
) {}
