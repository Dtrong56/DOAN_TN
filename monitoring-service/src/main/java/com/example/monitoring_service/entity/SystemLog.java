package com.example.monitoring_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_log",
       indexes = {
           @Index(name = "idx_syslog_action", columnList = "action"),
           @Index(name = "idx_syslog_tenant", columnList = "tenant_id"),
           @Index(name = "idx_syslog_user", columnList = "user_id")
       })
public class SystemLog {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

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

    // --- Getters & Setters ---
    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

