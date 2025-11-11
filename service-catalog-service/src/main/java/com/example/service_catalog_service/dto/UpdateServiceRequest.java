package com.example.service_catalog_service.dto;

import java.util.List;

public record UpdateServiceRequest(
        String name,
        String description,
        String unit,
        List<UpdateServicePackageRequest> packages
) {}

