package com.example.monitoring_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "system_log",
       indexes = {
           @Index(name = "idx_syslog_action", columnList = "action"),
           @Index(name = "idx_syslog_tenant", columnList = "tenant_id"),
           @Index(name = "idx_syslog_user", columnList = "user_id")
       })
public class SystemLog extends BaseEntity {

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "tenant_id", length = 36)
    private String tenantId;

    @Column(length = 100, nullable = false)
    private String action;

    @Column(name = "object_type", length = 50)
    private String objectType;

    @Column(name = "object_id", length = 100)
    private String objectId;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- Constructors ---
    public SystemLog() {}

    public SystemLog(String action, String userId, String tenantId, String objectType, String objectId, String description) {
        this.action = action;
        this.userId = userId;
        this.tenantId = tenantId;
        this.objectType = objectType;
        this.objectId = objectId;
        this.description = description;
    }
}

