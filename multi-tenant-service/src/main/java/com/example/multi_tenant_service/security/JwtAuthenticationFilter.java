package com.example.multi_tenant_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        // ✅ Lấy token từ header Authorization
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);
        }

        // ✅ Xử lý khi token hợp lệ và chưa có Authentication trong context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtService.validateToken(jwt)) {
                // 🔹 Lấy các thông tin từ JWT
                String userId = jwtService.extractUserId(jwt);
                String tenantId = jwtService.extractTenantId(jwt);
                List<String> roles = jwtService.extractRoles(jwt);

                // 🔹 Chuyển roles -> GrantedAuthority
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // 🔹 Tạo authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                // ✅ Gắn thêm details để debug/log dễ hơn
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ✅ Đặt Authentication vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // 🔹 Ghi log kiểm tra (chỉ để dev)
                System.out.printf("[JWT ✅] Authenticated user=%s | userId=%s | tenantId=%s | roles=%s%n",
                        username, userId, tenantId, roles);
            }
        }

        // ✅ Tiếp tục chuỗi filter
        filterChain.doFilter(request, response);
    }

    // ❌ Không chặn route nào — mọi route đều có thể nhận JWT
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return false;
    }
}
