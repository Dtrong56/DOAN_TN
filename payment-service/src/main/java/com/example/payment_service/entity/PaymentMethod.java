package com.example.payment_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "payment_method",
    uniqueConstraints = @UniqueConstraint(name = "uq_paymethod_tenant_code", columnNames = {"tenant_id", "method_code"})
)
public class PaymentMethod {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "method_code", length = 50, nullable = false)
    private String methodCode;

    @Column(name = "display_name", length = 100, nullable = false)
    private String displayName;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters & setters
}

