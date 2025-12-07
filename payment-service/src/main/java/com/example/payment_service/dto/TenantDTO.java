package com.example.payment_service.dto;

import lombok.Data;

@Data
public class TenantDTO {
    private String id;
    private String name;
    private boolean active;
}
