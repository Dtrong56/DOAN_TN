package com.example.auth_service.service;

import com.example.auth_service.dto.*;
import com.example.auth_service.entity.*;
import com.example.auth_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserBulkService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /*
     * Tạo nhiều user cho cư dân mới
     */
    @Transactional(rollbackFor = Exception.class)
    public AuthBulkCreateResponse bulkCreate(List<AuthUserCreateRequest> requests) {

        Role residentRole = roleRepository.findByName("RESIDENT")
        .orElseGet(() -> {
            Role newRole = Role.builder().name("RESIDENT").build();
            return roleRepository.save(newRole);
        });

        List<AuthCreateResult> results = new ArrayList<>();

        for (AuthUserCreateRequest req : requests) {
            try {
                // 1) Generate username from name + last4(cccd)
                String username = generateUsername(req.getFullName(), req.getCccd());

                // 2) Check user exists by username ONLY
                Optional<User> existing = userRepository.findByUsername(username);
                User user;
                if (existing.isPresent()) {
                    user = existing.get();
                } else {
                    // 3) Create new User
                    user = User.builder()
                            .username(username)
                            .email(req.getEmail())
                            .active(true)
                            .firstLogin(true)
                            .build();
                    userRepository.save(user);

                    // 4) Create default credential
                    Credential credential = Credential.builder()
                        .user(user)
                        .hashedPassword(passwordEncoder.encode("123456")) // ✅ dùng bcrypt
                        .lastPasswordChange(null) // lần đầu đăng nhập
                        .build();
                    credentialRepository.save(credential);

                    // 5) Assign RESIDENT role
                    UserRole userRole = UserRole.builder()
                            .user(user)
                            .role(residentRole)
                            .build();
                    userRoleRepository.save(userRole);
                }

                results.add(
                        new AuthCreateResult(req.getCccd(), user.getId(), null)
                );

            } catch (Exception e) {
                results.add(
                        new AuthCreateResult(req.getCccd(), null, e.getMessage())
                );
            }
        }

        AuthBulkCreateResponse resp = new AuthBulkCreateResponse();
        resp.setResults(results);
        resp.setSuccess(results.stream().noneMatch(r -> r.getError() != null));
        return resp;
    }

    /*
     * Chuyển user: vô hiệu hóa user cũ (nếu có) và tạo user mới
     */
    @Transactional(rollbackFor = Exception.class)
    public AuthCreateResult transferUser(AuthTransferUserRequest req) {
        try {
            // 1) Vô hiệu hóa user cũ
            if (req.getOldUserId() != null) {
                userRepository.findById(req.getOldUserId()).ifPresent(oldUser -> {
                    oldUser.setActive(false);
                    userRepository.save(oldUser);
                });
            }

            // 2) Tạo user mới như trong bulkCreate
            String username = generateUsername(req.getFullName(), req.getCccd());

            User user = User.builder()
                    .username(username)
                    .email(req.getEmail())
                    .active(true)
                    .firstLogin(true)
                    .build();
            userRepository.save(user);

            Credential credential = Credential.builder()
                    .user(user)
                    .hashedPassword(passwordEncoder.encode("123456"))
                    .build();
            credentialRepository.save(credential);

            Role residentRole = roleRepository.findByName("RESIDENT")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("RESIDENT").build()));

            UserRole userRole = UserRole.builder()
                    .user(user)
                    .role(residentRole)
                    .build();
            userRoleRepository.save(userRole);

            return new AuthCreateResult(req.getCccd(), user.getId(), null);

        } catch (Exception e) {
            return new AuthCreateResult(req.getCccd(), null, e.getMessage());
        }
    }

    //Các hàm hỗ trợ
    private String normalizeName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", "_")
                .trim();
    }

    private String generateUsername(String fullName, String cccd) {
        String base = normalizeName(fullName) + "_" + cccd.substring(cccd.length() - 4);
        String username = base;
        int suffix = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + "_" + suffix;
            suffix++;
        }
        return username;
    }
}
