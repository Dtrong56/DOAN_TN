package com.example.resident_service.controller;

import com.example.resident_service.dto.ResidentAccountInternalResponse;
import com.example.resident_service.dto.ResidentAccountResponse;
import com.example.resident_service.service.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/resident-accounts")
@RequiredArgsConstructor
public class InternalResidentAccountController {

    private final ResidentService ResidentService;

    /**
     * API nội bộ cho AuthService gọi để lấy tenantId của cư dân theo userId.
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ResidentAccountResponse> getTenantByUser(@PathVariable String userId) {
        ResidentAccountResponse response = ResidentService.getTenantByUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * API nội bộ cho contract-service gọi bằng Feign
     */
    @GetMapping("/by-user-contract/{userId}")
    public ResponseEntity<ResidentAccountInternalResponse> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(ResidentService.getResidentInfoByUserId(userId));
    }
}
