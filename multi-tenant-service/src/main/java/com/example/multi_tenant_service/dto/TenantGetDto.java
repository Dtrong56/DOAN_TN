package com.example.multi_tenant_service.dto;

import com.example.multi_tenant_service.entity.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantGetDto {
    private String id;
    private String name;
    private String address;
    private String contactName;
    private String contactEmail;
    private TenantStatus status;

    // Danh sách ID của các ManagementAccount
    private List<String> managementAccountIds;

    // Danh sách ID của các ManagementProfile (nếu có)
    private List<String> managementProfileIds;
}
