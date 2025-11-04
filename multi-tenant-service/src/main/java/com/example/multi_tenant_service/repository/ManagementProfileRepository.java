package com.example.multi_tenant_service.repository;

import com.example.multi_tenant_service.entity.ManagementProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagementProfileRepository extends JpaRepository<ManagementProfile, String> {
    // có thể bổ sung query nếu cần (VD: tìm theo email hoặc tenantId)
    ManagementProfile findByEmail(String email);
}
