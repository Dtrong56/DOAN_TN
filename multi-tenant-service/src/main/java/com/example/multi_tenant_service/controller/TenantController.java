package com.example.multi_tenant_service.controller;

import com.example.multi_tenant_service.entity.Tenant;
import com.example.multi_tenant_service.service.TenantService;
import com.example.multi_tenant_service.dto.TenantCreateRequest;
import com.example.multi_tenant_service.dto.TenantResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;


import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    //Tạo mới tenant
    @PostMapping
    public TenantResponse createTenant(@RequestBody TenantCreateRequest request) {
        return tenantService.createTenant(request);
    }

    //Cập nhật trạng thái kích hoạt của tenant
    @PatchMapping("/{tenantId}/status")
    public ResponseEntity<TenantResponse> updateTenantStatus(
            @PathVariable String tenantId,
            @RequestParam boolean active,
            Authentication authentication) {

        String currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            System.out.println("JWT Claims: " + jwt.getClaims());
            currentUserId = jwt.getClaimAsString("userId");
        }

        TenantResponse response = tenantService.updateTenantStatus(tenantId, active, currentUserId);
        return ResponseEntity.ok(response);
    }

    //Cập nhật thông tin tenant
    @PutMapping("/{id}")
    public Tenant updateTenant(@PathVariable String id, @RequestBody Tenant tenant) {
        return tenantService.updateTenant(id, tenant);
    }

    // //Xóa tenant
    // @DeleteMapping("/{id}")
    // public void deleteTenant(@PathVariable String id) {
    //     tenantService.deleteTenant(id);
    // }

    //Lấy thông tin tenant theo ID
    @GetMapping("/{id}")
    public Tenant getTenantById(@PathVariable String id) {
        return tenantService.getTenantById(id);
    }

    //Lấy danh sách tất cả tenant
    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }
}
