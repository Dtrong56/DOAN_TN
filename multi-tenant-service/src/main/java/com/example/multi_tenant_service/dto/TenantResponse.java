package com.example.multi_tenant_service.dto;

import com.example.multi_tenant_service.entity.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {
    private String id;
    private String name;
    private String address;
    private String contactName;
    private String contactEmail;
    private String status;
    private String managementUserId; // id user BQL chính

    public static TenantResponse fromEntity(Tenant tenant, String managementUserId) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .address(tenant.getAddress())
                .contactName(tenant.getContactName())
                .contactEmail(tenant.getContactEmail())
                .status(tenant.getStatus().name())
                .managementUserId(managementUserId)
                .build();
    }
}
