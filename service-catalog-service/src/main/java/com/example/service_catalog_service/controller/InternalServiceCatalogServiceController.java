package com.example.service_catalog_service.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.service_catalog_service.entity.ServiceCatalog;
import com.example.service_catalog_service.entity.ServicePackage;
import com.example.service_catalog_service.repository.ServiceCatalogRepository;
import com.example.service_catalog_service.repository.ServicePackageRepository;

@RestController
@RequestMapping("/internal/service")
@RequiredArgsConstructor
public class InternalServiceCatalogServiceController {
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ServicePackageRepository servicePackageRepository;
    
    //endpoint Contract-service validate serviceId.
    @GetMapping("/internal/service/{serviceId}")
    public ResponseEntity<ServiceInfoDTO> getServiceInfo(@PathVariable String serviceId) {
        ServiceCatalog svc = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        return ResponseEntity.ok(
                new ServiceInfoDTO(
                        svc.getId(),
                        svc.getName(),
                        svc.isActive()
                )
        );
    }

    public record ServiceInfoDTO(
        String id,
        String name,
        boolean active
    ) {}

    //Validate packageId thuộc serviceId

    // Lấy price, durationMonths

    // Kiểm tra package active
    @GetMapping("/internal/service/{serviceId}/package/{packageId}")
    public ResponseEntity<PackageInfoDTO> getPackageInfo(
            @PathVariable String serviceId,
            @PathVariable String packageId
    ) {

        ServicePackage pkg = servicePackageRepository
                .findByIdAndServiceCatalogId(packageId, serviceId)
                .orElseThrow(() -> new RuntimeException("Package not found for this service"));

        return ResponseEntity.ok(
                new PackageInfoDTO(
                        pkg.getId(),
                        pkg.getServiceCatalog().getId(),
                        pkg.getName(),
                        pkg.getDurationMonths(),
                        pkg.getPrice(),
                        pkg.isActive()
                )
        );
    }

    public record PackageInfoDTO(
        String id,
        String serviceId,
        String name,
        Integer durationMonths,
        BigDecimal price,
        boolean active
    ) {}


}
