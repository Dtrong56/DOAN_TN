package com.example.service_catalog_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registration_request")
public class RegistrationRequest {

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "resident_id", length = 36, nullable = false)
    private String residentId;

    @Column(name = "apartment_id", length = 36, nullable = false)
    private String apartmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_package_id", nullable = false)
    private ServicePackage servicePackage;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "approved_by_user_id", length = 36)
    private String approvedByUserId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    // Getters & setters
}

