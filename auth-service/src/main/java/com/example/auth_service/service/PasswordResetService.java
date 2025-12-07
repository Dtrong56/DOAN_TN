package com.example.auth_service.service;

import com.example.auth_service.client.MonitoringClient;
import com.example.auth_service.client.NotificationClient;
import com.example.auth_service.dto.NotificationRequestDTO;
import com.example.auth_service.dto.SystemLogDTO;
import com.example.auth_service.entity.Credential;
import com.example.auth_service.entity.PasswordResetToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.CredentialRepository;
import com.example.auth_service.repository.PasswordResetTokenRepository;
import com.example.auth_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final CredentialRepository credentialRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient; // Feign client gửi email
    private final MonitoringClient monitoringClient;

    private static final long EXPIRATION_MINUTES = 15;

    // Step 1: request reset
    public void requestPasswordReset(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // Generate token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        String resetLink = "https://your-domain.com/reset-password?token=" + token;

        // ... trong phương thức requestPasswordReset(...)
        NotificationRequestDTO notification = NotificationRequestDTO.builder()
                .tenantId(null) // nếu có tenantId thì set vào đây
                .userId(user.getId())
                .recipientEmail(user.getEmail())
                .title("Đặt lại mật khẩu")
                .message("Nhấn vào liên kết để đặt lại mật khẩu: " + resetLink)
                .type("PASSWORD_RESET")
                .objectType("User")
                .objectId(user.getId())
                .action("REQUEST_PASSWORD_RESET")
                .channelType("EMAIL")
                .metadata(Map.of("resetLink", resetLink, "token", token))
                .build();

        notificationClient.sendNotification(notification);

        // Log
        monitoringClient.createLog(
                SystemLogDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .userId(user.getId())
                    .tenantId(null)                         // Auth service không có tenant → để null
                    .role(null)                             // Không xác định trong flow này
                    .action("REQUEST_PASSWORD_RESET")
                    .objectType("User")
                    .objectId(user.getId())
                    .message("User requested password reset token")
                    .metadata(Map.of(
                        "email", user.getEmail()
                    ))
                    .serviceName("auth-service")
                    .endpoint("/api/auth/forgot-password")
                    .traceId(UUID.randomUUID().toString())
                    .build()
        );

    }

    // Step 2: handle reset password
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (resetToken.getUsed()) {
            throw new RuntimeException("Token đã được sử dụng");
        }

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Token đã hết hạn");
        }

        User user = resetToken.getUser();

        // Update credential
        Credential credential = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("User credential not found"));

        credential.setHashedPassword(passwordEncoder.encode(newPassword));
        credential.setLastPasswordChange(Instant.now());
        credentialRepository.save(credential);

        // Mark token used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Log
        monitoringClient.createLog(
                SystemLogDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .userId(user.getId())
                    .tenantId(null)
                    .role(null)
                    .action("RESET_PASSWORD")
                    .objectType("User")
                    .objectId(user.getId())
                    .message("User successfully reset password")
                    .metadata(Map.of(
                        "tokenUsed", true
                    ))
                    .serviceName("auth-service")
                    .endpoint("/api/auth/reset-password")
                    .traceId(UUID.randomUUID().toString())
                    .build()
        );
    }
}
