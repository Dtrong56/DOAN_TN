package com.example.contract_service.controller.internal;

import com.example.contract_service.dto.ServiceAppendixDTO;
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
            ServiceAppendixDTO dto = new ServiceAppendixDTO();
            dto.setAppendixId(a.getId());
            dto.setServiceId(a.getServiceId());
            dto.setPackageId(a.getPackageId());
            dto.setServiceName(a.getServiceName());     // ĐÃ CÓ trong entity sửa rồi
            dto.setPackageName(a.getPackageName());     // ĐÃ CÓ trong entity sửa rồi
            dto.setUnitPrice(a.getUnitPrice());         // ĐÃ CÓ trong entity sửa rồi
            dto.setStartDate(a.getEffectiveDate());
            dto.setEndDate(a.getExpirationDate());
            return dto;
        }).toList();
    }
}
