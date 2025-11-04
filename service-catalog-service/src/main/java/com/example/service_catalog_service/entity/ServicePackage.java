package com.example.service_catalog_service.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "service_package")
public class ServicePackage {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceCatalog serviceCatalog;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    // Getters & setters
}
