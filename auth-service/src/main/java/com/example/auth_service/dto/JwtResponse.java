package com.example.auth_service.dto;

import java.util.List;

public class JwtResponse {
    private String token;
    private String username;
    private List<String> roles;
    private String tenantId;
    private String expiresAt;
    private Boolean firstLogin;

    public JwtResponse(String token, String username, List<String> roles, String tenantId, String expiresAt, Boolean firstLogin) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.tenantId = tenantId;
        this.expiresAt = expiresAt;
        this.firstLogin = firstLogin;
    }

    // getters/setters
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public List<String> getRoles() {
        return roles;
    }
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public String getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(Boolean firstLogin) {
        this.firstLogin = firstLogin;
    }
}
