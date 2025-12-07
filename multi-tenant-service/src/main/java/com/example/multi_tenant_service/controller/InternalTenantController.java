package com.example.multi_tenant_service.controller;

import com.example.multi_tenant_service.entity.Tenant;
import com.example.multi_tenant_service.entity.TenantStatus;
import com.example.multi_tenant_service.dto.TenantDTO;
import com.example.multi_tenant_service.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/internal/tenants")
@RequiredArgsConstructor
public class InternalTenantController {

    private final TenantRepository tenantRepository;

    @GetMapping("/active")
    public List<TenantDTO> getActiveTenants() {
        List<Tenant> tenants = tenantRepository.findByStatus(TenantStatus.ACTIVE);
        return tenants.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private TenantDTO toDTO(Tenant tenant) {
        TenantDTO dto = new TenantDTO();
        dto.setId(tenant.getId()); // giả sử BaseEntity có trường id kiểu String/UUID
        dto.setName(tenant.getName());
        dto.setActive(tenant.getStatus() == TenantStatus.ACTIVE);
        return dto;
    }
}