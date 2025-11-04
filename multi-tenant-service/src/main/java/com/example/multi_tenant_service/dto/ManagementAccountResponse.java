package com.example.multi_tenant_service.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagementAccountResponse {
    private String tenantId;
    private String tenantName;
    private String position;
    private String fullName;
    private String phone;
    private String email;
}
