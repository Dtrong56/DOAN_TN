package com.example.auth_service.controller;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.ValidateTokenRequest;
import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.dto.DigitalSignatureInfoResponse;
import com.example.auth_service.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.auth_service.dto.CreateAdminRequest;
import java.util.Map;
import com.example.auth_service.security.JwtService;

import org.springframework.security.core.Authentication;
import com.example.auth_service.dto.UserResponse;
import com.example.auth_service.dto.ResetBqlRequest;
import com.example.auth_service.dto.ChangePasswordRequest;
import com.example.auth_service.dto.ChangePasswordResponse;
import com.example.auth_service.dto.DigitalSignatureUploadRequest;
import com.example.auth_service.dto.DigitalSignatureUploadResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;



@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        JwtResponse response = authService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }

    // Initial admin creation endpoint
    @PostMapping("/init-admin")
    public ResponseEntity<?> initAdmin(@RequestBody CreateAdminRequest request) {
        return ResponseEntity.ok(authService.createInitialAdmin(request));
    }

    // Token validation endpoint
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody ValidateTokenRequest request) {
        System.out.println("🔑 [AuthService] Validate token request: " + request.getToken());
        boolean isValid = authService.validateToken(request.getToken());
        System.out.println("✅ [AuthService] Token valid? " + isValid);
        return ResponseEntity.ok(isValid);
    }

    // Reset BQL account endpoint
    @PutMapping("/v1/users/reset")
    public ResponseEntity<?> resetBqlAccount(@RequestBody ResetBqlRequest request) {

        // Lấy userId của người đang thao tác (admin) từ authentication.details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUserId = "system"; // fallback

        if (authentication != null && authentication.getDetails() instanceof Map<?,?> details) {
            Object val = details.get("userId");
            if (val != null) adminUserId = val.toString();
        }

        // Validate request
        if (request == null || request.getUserId() == null || request.getUserId().isBlank()
                || request.getTenantId() == null || request.getTenantId().isBlank()) {
            return ResponseEntity.badRequest().body("userId and tenantId are required");
        }

        UserResponse updated = authService.resetBqlAccount(request.getUserId(), request.getTenantId(), adminUserId);
        return ResponseEntity.ok(updated);
    }

    // Change password for the first time
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        // ✅ Lấy token từ header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        // ✅ Lấy userId từ token
        String userId = jwtService.extractUserId(token);

        // ✅ Gọi service xử lý đổi mật khẩu
        ChangePasswordResponse response = authService.changePassword(userId, request);

        return ResponseEntity.ok(response);
    }

    // Upload digital signature endpoint
    @PostMapping("/digital-signature/upload")
    public ResponseEntity<?> uploadSignature(
            @RequestPart("publicKeyFile") MultipartFile publicKey,
            @RequestPart(value = "certificateFile", required = false) MultipartFile certificate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo,
            @RequestHeader("Authorization") String authHeader
    ) {

        String token = authHeader.substring(7);
        String userId = jwtService.extractUserId(token);

        DigitalSignatureUploadRequest req = new DigitalSignatureUploadRequest();
        req.setPublicKeyFile(publicKey);
        req.setCertificateFile(certificate);
        req.setValidFrom(validFrom);
        req.setValidTo(validTo);

        DigitalSignatureUploadResponse response = authService.upload(userId, req);
        return ResponseEntity.ok(response);
    }

    //endpoint lấy signature
    @GetMapping("/digital-signature/info")
    public ResponseEntity<?> getSignatureInfo(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String userId = jwtService.extractUserId(token);

        DigitalSignatureInfoResponse response = authService.getDigitalSignatureInfo(userId);

        return ResponseEntity.ok(response);
    }


}

