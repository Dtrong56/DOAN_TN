package com.example.resident_service.controller;

import com.example.resident_service.dto.ResidentDTO;
import com.example.resident_service.service.InternalResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal/residents")
@RequiredArgsConstructor
public class InternalResidentController {

    private final InternalResidentService residentService;

    @GetMapping
    public List<ResidentDTO> getResidents(
            @RequestParam String tenantId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "true") boolean includeApartment
    ) {
        return residentService.getResidents(tenantId, activeOnly, includeApartment);
    }
}
