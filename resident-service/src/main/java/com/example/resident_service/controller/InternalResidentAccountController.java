package com.example.resident_service.controller;

import com.example.resident_service.dto.ResidentAccountResponse;
import com.example.resident_service.service.ResidentAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/resident-accounts")
@RequiredArgsConstructor
public class InternalResidentAccountController {

    private final ResidentAccountService residentAccountService;

    /**
     * API nội bộ cho AuthService gọi để lấy tenantId của cư dân theo userId.
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ResidentAccountResponse> getTenantByUser(@PathVariable UUID userId) {
        ResidentAccountResponse response = residentAccountService.getTenantByUser(userId);
        return ResponseEntity.ok(response);
    }
}
