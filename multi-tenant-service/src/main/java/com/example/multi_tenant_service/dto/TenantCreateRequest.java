package com.example.multi_tenant_service.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantCreateRequest {
    private String name;
    private String address;
    private String contactName;
    private String contactEmail;

    // Thông tin BQL
    private String fullName;
    private String email;
    private String phone;
    private String position;
    private String addressBql;
    private String avatarUrl;
    private String note;

    // Cấu hình tenant (tuỳ chọn)
    private Map<String, String> config;
}
