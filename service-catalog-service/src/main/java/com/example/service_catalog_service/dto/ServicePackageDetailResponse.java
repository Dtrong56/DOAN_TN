package com.example.service_catalog_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record ServicePackageDetailResponse(
        String id,
        String name,
        String description,
        Integer durationMonths,
        BigDecimal price,
        boolean active,
        List<PackagePriceHistoryResponse> priceHistory
) {}
