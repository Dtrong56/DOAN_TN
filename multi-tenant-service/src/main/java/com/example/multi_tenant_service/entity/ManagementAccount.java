package com.example.multi_tenant_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "management_account",
    uniqueConstraints = @UniqueConstraint(name = "uq_mgmt_tenant_user", columnNames = {"tenant_id", "user_id"}),
    indexes = {@Index(name = "idx_mgmt_tenant", columnList = "tenant_id")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagementAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String userId;

    @Column(nullable = false)
    private Boolean active = true;

    // 🔹 Thêm phần này để sửa lỗi getProfile()
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "management_profile_id")
    private ManagementProfile profile;
}
