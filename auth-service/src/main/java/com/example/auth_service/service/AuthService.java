package com.example.auth_service.service;

import com.example.auth_service.dto.ChangePasswordRequest;
import com.example.auth_service.dto.ChangePasswordResponse;
import com.example.auth_service.dto.CreateAdminRequest;
import com.example.auth_service.dto.CreateUserRequest;
import com.example.auth_service.dto.CreateUserResponse;
import com.example.auth_service.dto.DigitalSignatureInternalDTO;
import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.SystemLogDTO;
import com.example.auth_service.dto.UserResponse;
import com.example.auth_service.entity.Credential;
import com.example.auth_service.entity.User;
import com.example.auth_service.entity.UserRole;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.repository.UserRoleRepository;
import com.example.auth_service.repository.CredentialRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.DigitalSignatureRepository;
import com.example.auth_service.service.FileStorageService;
import com.example.auth_service.security.JwtService;
import com.example.auth_service.security.TenantContext;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.auth_service.entity.Role;
import com.example.auth_service.client.TenantClient;
import com.example.auth_service.client.ResidentClient;
import com.example.auth_service.client.MonitoringClient;
import com.example.auth_service.dto.DigitalSignatureUploadRequest;
import com.example.auth_service.dto.DigitalSignatureUploadResponse;
import com.example.auth_service.entity.DigitalSignature;
import com.example.auth_service.publisher.LogPublisher;





import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TenantClient tenantClient;
    private final ResidentClient residentClient;
    private final MonitoringClient monitoringClient;
    private final FileStorageService fileStorageService;
    private final DigitalSignatureRepository digitalSignatureRepository;
    private final TenantContext tenantContext;
    private final LogPublisher logPublisher;

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
        String residentId = null;
        Map resp = null;
        try {
            if (roles.contains("BQL")) {
                // Ban Quản Lý → gọi tenant-service để lấy tenantId
                tenantId = tenantClient.getTenantIdByManager(user.getId());
            } else if (roles.contains("RESIDENT")) {
                // Cư dân → gọi resident-service để lấy tenantId
                resp = residentClient.getTenantIdByResident(user.getId());
                tenantId = resp.get("tenantId").toString();
                residentId = resp.get("residentProfileId").toString();
            }
            // ADMIN → bỏ qua tenantId (Super Admin toàn hệ thống)
        } catch (Exception ex) {
            System.err.printf("[WARN] Cannot resolve tenant for user=%s (%s)%n", user.getUsername(), ex.getMessage());
        }

        // 6️⃣ Sinh JWT token (có thể kèm tenantId)
        String token = jwtService.generateToken(user.getId(), user.getUsername(), tenantId, roles, residentId);
        Date expires = jwtService.extractExpiration(token);

        // 7️⃣ Tạo response
        JwtResponse response = new JwtResponse(
                token,
                user.getUsername(),
                roles,
                tenantId,
                expires.toInstant().toString(),
                user.getFirstLogin() // ✅ thêm line này
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

    /**
     * Reset tài khoản BQL: đặt lại mật khẩu về "123456" và deactivate account.
     */
    @Transactional
    public UserResponse resetBqlAccount(String targetUserId, String targetTenantId, String actionUserId) {
        String roleName = tenantContext.getUserRoles();

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Khóa account
        user.setActive(false);
        userRepository.save(user);

        // Reset mật khẩu
        Credential credential = credentialRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Credential not found"));
        credential.setHashedPassword(passwordEncoder.encode("123456"));
        credentialRepository.save(credential);

        // ✅ Ghi log
        // monitoringClient.createLog(
        //     new SystemLogDTO(
        //         LocalDateTime.now(),
        //         tenantContext.getUserId(),
        //         targetTenantId,
        //         roleName,
        //         "RESET_BQL_ACCOUNT",
        //         "Credential",
        //         credential.getId(),
        //         "Reset BQL account and deactivated it",
        //         null,
        //         "AuthService",
        //         "/api/auth/reset-bql",
        //         null                
        //     )
        // );
        logPublisher.sendLog(
            new SystemLogDTO(
                LocalDateTime.now(),
                actionUserId,
                targetTenantId,
                roleName,
                "RESET_BQL_ACCOUNT",
                "Credential",
                credential.getId(),
                "Reset BQL account and deactivated it",
                null,
                "AuthService",
                "/api/auth/users/reset",
                null                
            )
        );

        return new UserResponse(user.getId(), user.getUsername(), false, "Account reset successful");
    }

    /**
     * Đổi mật khẩu cho user.
     */
   @Transactional
    public ChangePasswordResponse changePassword(String userId, ChangePasswordRequest request) {
        String tenantId = tenantContext.getTenantId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy credential hiện tại (LUÔN phải là unique)
        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Credential not found"));

        // Kiểm tra old password
        if (!passwordEncoder.matches(request.getOldPassword(), credential.getHashedPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // ✔ Update mật khẩu mới vào credential hiện tại
        credential.setHashedPassword(passwordEncoder.encode(request.getNewPassword()));
        credential.setLastPasswordChange(Instant.now());
        credentialRepository.save(credential);

        // ✔ Update user state
        user.setFirstLogin(false);
        user.setActive(true);
        userRepository.save(user);

        // Log (tùy chọn)
        monitoringClient.createLog(
            new SystemLogDTO(
                LocalDateTime.now(),
                tenantContext.getUserId(),
                tenantId,
                tenantContext.getUserRoles(),
                "CHANGE_PASSWORD",
                "Credential",
                credential.getId(),
                "Changed password successfully",
                null,
                "AuthService",
                "/api/auth/change-password",
                null                
            )
        );

        return new ChangePasswordResponse("Password changed successfully");
    }


    /**
     * Upload digital signature for user.
     */
    @Transactional
    public DigitalSignatureUploadResponse upload(String userId, DigitalSignatureUploadRequest req) {

        String publicPath;
        String certPath = null;
        String tenantId = tenantContext.getTenantId();

        try {
            // Lưu file public key
            publicPath = fileStorageService.save(req.getPublicKeyFile(), "public_keys");

            // Lưu file cert nếu có
            if (req.getCertificateFile() != null) {
                certPath = fileStorageService.save(req.getCertificateFile(), "certificates");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save signature files: " + e.getMessage(), e);
        }

        // Lưu vào DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DigitalSignature ds = new DigitalSignature();
        ds.setUser(user);
        ds.setPublicKeyPath(publicPath);
        ds.setCertFilePath(certPath);
        ds.setValidFrom(req.getValidFrom());
        ds.setValidTo(req.getValidTo());
        ds.setActive(true);

        // ❗ Bạn thiếu thuộc tính publicKeyPath trong entity
        ds.setPublicKeyPath(publicPath);

        digitalSignatureRepository.save(ds);

        monitoringClient.createLog(
            new SystemLogDTO(
                LocalDateTime.now(),
                tenantContext.getUserId(),
                tenantId,
                "Resident",
                "UPLOAD_DIGITAL_SIGNATURE",
                "Credential",
                ds.getId(),
                "Uploaded digital signature successfully",
                null,
                "AuthService",
                "/api/auth/upload-digital-signature",
                null                
            )
        );

        return new DigitalSignatureUploadResponse(
                ds.getId(),
                publicPath,
                certPath,
                ds.getValidFrom(),
                ds.getValidTo()
        );
    }

    /**
     * Lấy digital signature nội bộ của User
     * @throws IOException 
     */
    @Transactional(readOnly = true)
    public DigitalSignatureInternalDTO getDigitalSignatureInternal(String userId) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DigitalSignature sig = digitalSignatureRepository
                .findActiveByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No digital signature found for user"));

        String pem = Files.readString(Path.of(sig.getPublicKeyPath()));

        return new DigitalSignatureInternalDTO(
                pem,
                "SHA256withRSA",
                true
        );
    }
}

