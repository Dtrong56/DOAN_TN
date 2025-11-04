package com.example.multi_tenant_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import jakarta.validation.constraints.Email;

// ManagementAccountCreateRequest.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagementAccountCreateRequest {
    @NotBlank
    private String fullName;
    @NotBlank
    private String username;
    @Email
    private String email;
    @NotBlank
    private String password;
}
