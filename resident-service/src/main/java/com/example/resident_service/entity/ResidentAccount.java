package com.example.resident_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;

@Entity
@Table(name = "resident_account",
       indexes = {
           @Index(name = "idx_racct_tenant", columnList = "tenant_id"),
           @Index(name = "idx_racct_user", columnList = "user_id"),
           @Index(name = "idx_racct_profile", columnList = "resident_profile_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentAccount extends BaseEntity {

    @Column(name = "tenant_id", columnDefinition = "CHAR(36)", nullable = false)
    private String tenantId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_profile_id", nullable = false)
    private ResidentProfile residentProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResidentAccountStatus status = ResidentAccountStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
