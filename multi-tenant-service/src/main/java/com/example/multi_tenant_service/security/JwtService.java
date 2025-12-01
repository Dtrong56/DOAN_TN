package com.example.multi_tenant_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey123"; // Phải trùng với Auth Service

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ✅ Chỉ dùng để xác thực token nhận từ Auth Service
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // ✅ Lấy toàn bộ payload
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ Lấy username
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // ✅ Lấy userId
    public String extractUserId(String token) {
        return (String) parseClaims(token).get("userId");
    }

    // ✅ Lấy tenantId
    public String extractTenantId(String token) {
        return (String) parseClaims(token).get("tenantId");
    }

    // ✅ Lấy residentId
    public String extractResidentId(String token) {
        return (String) parseClaims(token).get("residentId");
    }

    // ✅ Lấy danh sách roles
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) parseClaims(token).get("roles");
    }

    // ✅ Lấy thời hạn token
    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }
}
