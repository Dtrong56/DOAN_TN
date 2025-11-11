package com.example.service_catalog_service.dto;

import java.util.List;

public record ServiceCatalogDetailResponse(
        String id,
        String name,
        String description,
        String unit,
        boolean active,
        List<ServicePackageDetailResponse> packages
) {}
