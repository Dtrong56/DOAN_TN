package com.example.service_catalog_service.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "service_catalog")
public class ServiceCatalog {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50, nullable = false)
    private String unit;

    @Column(nullable = false)
    private boolean active = true;

    // Getters & setters
}
