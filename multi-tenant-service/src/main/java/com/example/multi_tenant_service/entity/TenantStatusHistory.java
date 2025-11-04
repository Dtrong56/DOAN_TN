package com.example.multi_tenant_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;

@Entity
@Table(name = "tenant_status_history",
       indexes = {@Index(name = "idx_tsh_tenant", columnList = "tenant_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "old_status", length = 20)
    private String oldStatus;

    @Column(name = "new_status", length = 20, nullable = false)
    private String newStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt = Instant.now();

    @Column(name = "changed_by_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String changedByUserId;

    @Column(columnDefinition = "TEXT")
    private String note;
}
