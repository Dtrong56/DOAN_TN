package com.example.multi_tenant_service.dto;

import lombok.Data;

@Data
public class TenantDTO {
    private String id;
    private String name;
    private boolean active;
}
