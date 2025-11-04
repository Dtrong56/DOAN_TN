package com.example.auth_service.service;

import com.example.auth_service.dto.CreateAdminRequest;
import com.example.auth_service.dto.CreateUserRequest;
import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.entity.Credential;
import com.example.auth_service.entity.User;
import com.example.auth_service.entity.UserRole;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.repository.UserRoleRepository;
import com.example.auth_service.repository.CredentialRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.auth_service.entity.Role;
import com.example.auth_service.client.TenantClient;
import com.example.auth_service.client.ResidentClient;


import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TenantClient tenantClient;
    private final ResidentClient residentClient;

    

    public AuthService(UserRepository userRepository,
                       CredentialRepository credentialRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService, 
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       TenantClient tenantClient,
                       ResidentClient residentClient) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.tenantClient = tenantClient;
        this.residentClient = residentClient;
    }

    /**
     * Xử lý đăng nhập, xác thực username/password và sinh JWT token.
     * - Nếu là ADMIN (Super Admin): JWT không chứa tenantId.
     * - Nếu là BQL hoặc RESIDENT: tự động xác định tenantId tương ứng.
     */
    public JwtResponse authenticateUser(LoginRequest request) {
        // 1️⃣ Kiểm tra tồn tại user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("User account is inactive");
        }

        // 2️⃣ Lấy credential mới nhất
        Optional<Credential> optCred = user.getCredentials().stream()
                .max(Comparator.comparing(
                        Credential::getLastPasswordChange,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));

        Credential credential = optCred.orElseThrow(() -> new RuntimeException("Credential not found"));

        // 3️⃣ Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), credential.getHashedPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 4️⃣ Lấy danh sách role của user
        var roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        // 5️⃣ Xác định tenantId (nếu cần)
        String tenantId = null;
        try {
            if (roles.contains("BQL")) {
                // Ban Quản Lý → gọi tenant-service để lấy tenantId
                tenantId = tenantClient.getTenantIdByManager(user.getId());
            } else if (roles.contains("RESIDENT")) {
                // Cư dân → gọi resident-service để lấy tenantId
                tenantId = residentClient.getTenantIdByResident(user.getId());
            }
            // ADMIN → bỏ qua tenantId (Super Admin toàn hệ thống)
        } catch (Exception ex) {
            System.err.printf("[WARN] Cannot resolve tenant for user=%s (%s)%n", user.getUsername(), ex.getMessage());
        }

        // 6️⃣ Sinh JWT token (có thể kèm tenantId)
        String token = jwtService.generateToken(user.getId(), user.getUsername(), tenantId, roles);
        Date expires = jwtService.extractExpiration(token);

        // 7️⃣ Tạo response
        JwtResponse response = new JwtResponse(
                token,
                user.getUsername(),
                roles,
                tenantId != null ? tenantId.toString() : null,
                expires.toInstant().toString()
        );

        // 8️⃣ Ghi log (tạm thời)
        System.out.printf("[MONITORING] LOGIN_USER: user=%s tenant=%s time=%s%n",
                user.getUsername(),
                tenantId != null ? tenantId : "GLOBAL",
                Instant.now());

        return response;
    }


     /**
     * Tạo tài khoản Super Admin hệ thống (chạy 1 lần duy nhất).
     */
    @Transactional
    public User createInitialAdmin(CreateAdminRequest request) {
        // 1️⃣ Kiểm tra đã có user ADMIN nào chưa
        boolean adminExists = userRoleRepository.existsByRole_Name("ADMIN");
        if (adminExists) {
            throw new RuntimeException("Super Admin already exists!");
        }

        // 2️⃣ Tạo Role ADMIN nếu chưa có
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    return roleRepository.save(role);
                });

        // 3️⃣ Tạo User
        User admin = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .active(true)
                .build();
        userRepository.save(admin);

        // 4️⃣ Lưu Credential
        Credential credential = Credential.builder()
                .user(admin)
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .lastPasswordChange(Instant.now())
                .build();
        credentialRepository.save(credential);

        // 5️⃣ Gán Role ADMIN cho User
        UserRole userRole = new UserRole();
        userRole.setUser(admin);
        userRole.setRole(adminRole);
        userRoleRepository.save(userRole);

        System.out.printf("[SYSTEM] ✅ Super Admin '%s' created successfully!%n", admin.getUsername());

        return admin;
    }
        /**
         * Validate JWT token.
         */
        public boolean validateToken(String token) {
            return jwtService.validateToken(token);
        }
    
    /**
     * Cập nhật trạng thái active của User
     */
    @Transactional
    public void updateUserActiveStatus(String userId, boolean active) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setActive(active);
        userRepository.save(user);

        System.out.printf("[SYSTEM] 🔄 Updated active=%s for user %s%n", active, user.getUsername());
    }
}
