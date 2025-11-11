package com.example.service_catalog_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PackagePriceHistoryResponse(
        BigDecimal oldPrice,
        BigDecimal newPrice,
        LocalDate effectiveFrom,
        LocalDateTime changedAt,
        String changedByUserId
) {}
