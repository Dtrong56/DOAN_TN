package com.example.auth_service.controller;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.ValidateTokenRequest;
import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.auth_service.dto.CreateAdminRequest;
import com.example.auth_service.dto.CreateUserRequest;
import com.example.auth_service.entity.User;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

    // // Tenant user creation endpoint
    // @PostMapping("/create-user")
    // public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
    //     User user = authService.createTenantUser(request);
    //     return ResponseEntity.ok(Map.of(
    //         "userId", user.getId(),
    //         "username", user.getUsername()
    //     ));
    // }    
}

