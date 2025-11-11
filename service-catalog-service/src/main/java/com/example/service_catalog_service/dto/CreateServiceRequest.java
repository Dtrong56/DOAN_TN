package com.example.service_catalog_service.dto;

import java.util.List;

public record CreateServiceRequest(
        String name,
        String description,
        String unit,
        List<ServicePackageRequest> packages
) {}

