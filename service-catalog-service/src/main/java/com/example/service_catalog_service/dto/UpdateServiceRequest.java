package com.example.service_catalog_service.dto;

import java.util.List;

public record UpdateServiceRequest(
        String id,
        String name,
        String description,
        String unit,
        Boolean active,
        List<UpdateServicePackageRequest> packages
) {}

