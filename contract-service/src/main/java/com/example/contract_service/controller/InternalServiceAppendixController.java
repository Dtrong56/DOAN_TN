package com.example.contract_service.controller;

import com.example.contract_service.client.CatalogClient;
import com.example.contract_service.dto.PackageInfoDTO;
import com.example.contract_service.dto.ServiceAppendixDTO;
import com.example.contract_service.dto.ServiceInfoDTO;
import com.example.contract_service.repository.ServiceAppendixRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/internal/service-appendices")
@RequiredArgsConstructor
public class InternalServiceAppendixController {

    private final ServiceAppendixRepository repository;
    private final CatalogClient catalogClient;

    @GetMapping("/active")
    public List<ServiceAppendixDTO> getActiveAppendices(
            @RequestParam String tenantId,
            @RequestParam String residentId,
            @RequestParam int periodMonth,
            @RequestParam int periodYear
    ) {
        LocalDate firstDay = LocalDate.of(periodYear, periodMonth, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        var list = repository.findActiveForPeriod(
                tenantId, residentId, firstDay, lastDay
        );

        return list.stream().map(a -> {
            // Lấy service info từ catalog
            ServiceInfoDTO serviceInfo = catalogClient.getServiceInfo(a.getServiceId());
            if (!serviceInfo.isActive()) {
                throw new RuntimeException("Service is inactive");
            }

            // Lấy package info từ catalog
            PackageInfoDTO packageInfo = catalogClient.getPackageOfService(a.getServiceId(), a.getPackageId());
            if (!packageInfo.isActive()) {
                throw new RuntimeException("Package is inactive");
            }

            ServiceAppendixDTO dto = new ServiceAppendixDTO();
            dto.setAppendixId(a.getId());
            dto.setServiceId(a.getServiceId());
            dto.setPackageId(a.getPackageId());
            dto.setServiceName(serviceInfo.getName());   // lấy tên từ catalog
            dto.setPackageName(packageInfo.getName());   // lấy tên từ catalog
            dto.setUnitPrice(a.getPrice());
            dto.setStartDate(a.getEffectiveDate());
            dto.setEndDate(a.getExpirationDate());
            return dto;
        }).toList();
    }
}
