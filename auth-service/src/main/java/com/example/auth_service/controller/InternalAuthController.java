package com.example.auth_service.controller;

import com.example.auth_service.dto.CreateUserRequest;
import com.example.auth_service.dto.CreateUserResponse;
import com.example.auth_service.dto.DigitalSignatureInternalDTO;
import com.example.auth_service.entity.*;
import com.example.auth_service.repository.*;
import com.example.auth_service.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class InternalAuthController {
    @Autowired
    private final AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * API nội bộ: Tạo User mới
     * @param req
     * @return
     */
    @PostMapping("/create-user")
    public CreateUserResponse createUser(@RequestBody CreateUserRequest req) {
        // 1️⃣ Kiểm tra username hoặc email đã tồn tại
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // 2️⃣ Tạo User
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setActive(false);
        userRepository.save(user);

        // 3️⃣ Tạo Credential (hash password)
        Credential credential = new Credential();
        credential.setUser(user);
        credential.setHashedPassword(passwordEncoder.encode(req.getPassword()));
        credential.setLastPasswordChange(Instant.now());
        credentialRepository.save(credential);

        // 4️⃣ Gán Role cho User
        Role role = roleRepository.findByName(req.getRole())
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName(req.getRole());
                return roleRepository.save(newRole);
            });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        // 5️⃣ Trả về kết quả
        return CreateUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(role.getName())
                .build();
    }

    /**
     * API nội bộ: Kiểm tra username đã tồn tại
     */
    @GetMapping("/check-username")
    public boolean checkUsernameExists(@RequestParam String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * API nội bộ: Cập nhật trạng thái active của User
     */
    @PutMapping("/update-active/{userId}")
    public ResponseEntity<?> updateUserActiveStatus(
            @PathVariable String userId,
            @RequestParam boolean active
    ) {
        authService.updateUserActiveStatus(userId, active);
        return ResponseEntity.ok("User " + userId + " active=" + active);
    }
    
    /**
     * API nội bộ: Lấy digital signature của User
     * @throws IOException 
     */
    @GetMapping("/digital-signature/{userId}")
    public ResponseEntity<DigitalSignatureInternalDTO> getSignature(@PathVariable String userId) throws IOException {
        return ResponseEntity.ok(authService.getDigitalSignatureInternal(userId));
    }

    /**
     * API nội bộ: lấy email theo userId
     */
    @GetMapping("/email/{userId}")
    public String getUserEmail(@PathVariable("userId") String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return user.getEmail();
    }
}
