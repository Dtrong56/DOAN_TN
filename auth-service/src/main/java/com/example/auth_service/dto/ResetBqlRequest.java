package com.example.auth_service.dto;

public class ResetBqlRequest {
    private String userId;   // target user to reset
    private String tenantId; // tenant of the target user

    public ResetBqlRequest() {}

    public ResetBqlRequest(String userId, String tenantId) {
        this.userId = userId;
        this.tenantId = tenantId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
