package com.example.multi_tenant_service.controller;

import com.example.multi_tenant_service.dto.ManagementAccountResponse;
import com.example.multi_tenant_service.service.ManagementAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/management-accounts")
@RequiredArgsConstructor
public class InternalManagementAccountController {

    private final ManagementAccountService managementAccountService;

    /**
     * API nội bộ dùng cho AuthService:
     * Trả về tenantId và thông tin cơ bản của BQL theo userId.
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ManagementAccountResponse> getTenantByUser(@PathVariable UUID userId) {
        ManagementAccountResponse response = managementAccountService.getTenantByUserId(userId);
        return ResponseEntity.ok(response);
    }
}
