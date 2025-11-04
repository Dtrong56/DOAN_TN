package com.example.multi_tenant_service.service;

import com.example.multi_tenant_service.client.AuthClient;
import com.example.multi_tenant_service.dto.CreateUserRequest;
import com.example.multi_tenant_service.dto.CreateUserResponse;
import com.example.multi_tenant_service.dto.TenantCreateRequest;
import com.example.multi_tenant_service.dto.TenantResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import com.example.multi_tenant_service.entity.*;
import com.example.multi_tenant_service.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Random;


import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;


@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantConfigRepository tenantConfigRepository;
    private final ManagementAccountRepository managementAccountRepository;
    private final ManagementProfileRepository managementProfileRepository;
    private final AuthClient authClient;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final TenantStatusHistoryRepository tenantStatusHistoryRepository;
    

    // @Autowired
    // public TenantService(
    //         TenantRepository tenantRepository,
    //         TenantConfigRepository tenantConfigRepository,
    //         ManagementAccountRepository managementAccountRepository,
    //         ManagementProfileRepository managementProfileRepository,
    //         AuthClient authClient,
    //         RabbitTemplate rabbitTemplate,
    //         ObjectMapper objectMapper) {
    //     this.tenantRepository = tenantRepository;
    //     this.tenantConfigRepository = tenantConfigRepository;
    //     this.managementAccountRepository = managementAccountRepository;
    //     this.managementProfileRepository = managementProfileRepository;
    //     this.authClient = authClient;
    //     this.rabbitTemplate = rabbitTemplate;
    //     this.objectMapper = objectMapper;
    // }

    @Transactional
    public TenantResponse createTenant(TenantCreateRequest request) {

        // 1️⃣ Tạo Tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setAddress(request.getAddress());
        tenant.setContactName(request.getContactName());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setStatus(TenantStatus.ACTIVE);
        Tenant savedTenant = tenantRepository.save(tenant);

        // 2️⃣ Tạo ManagementProfile
        ManagementProfile profile = ManagementProfile.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .position(request.getPosition())
                .address(request.getAddressBql())
                .avatarUrl(request.getAvatarUrl())
                .note(request.getNote())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        managementProfileRepository.save(profile);

        // 3️⃣ Sinh username duy nhất (auto retry)
        String base = generateUsernameBase(request.getFullName(), savedTenant.getName());
        String username = base;
        int retryCount = 0;
        final int maxRetries = 5;
        String defaultPassword = "123456";

        CreateUserResponse userResp = null;
        while (retryCount < maxRetries) {
            try {
                CreateUserRequest createUserReq = CreateUserRequest.builder()
                        .username(username)
                        .password(defaultPassword)
                        .email(request.getEmail())
                        .role("BQL")
                        .build();

                userResp = authClient.createUser(createUserReq);
                break; // Tạo user thành công -> thoát khỏi vòng lặp

            } catch (FeignException.FeignClientException e) {
                String body = e.contentUTF8();
                if (e.status() == 400 && body.contains("Username already exists")) {
                    // 🔁 Nếu trùng username -> thử tên khác
                    retryCount++;
                    username = base + retryCount;
                    log.warn("Username '{}' bị trùng, thử lại '{}'", base, username);
                } else {
                    // Lỗi khác thì dừng hẳn
                    throw new RuntimeException("AuthService error: " + e.getMessage(), e);
                }
            }
        }

        if (userResp == null) {
            throw new RuntimeException("Không thể tạo user quản lý sau " + maxRetries + " lần thử.");
        }

        String managerUserId = userResp.getUserId();

        // 4️⃣ Lưu ManagementAccount
        ManagementAccount account = new ManagementAccount();
        account.setTenant(savedTenant);
        account.setUserId(managerUserId);
        account.setActive(true);
        managementAccountRepository.save(account);

        // 5️⃣ TenantConfig mặc định
        if (request.getConfig() != null && !request.getConfig().isEmpty()) {
            for (Map.Entry<String, String> e : request.getConfig().entrySet()) {
                TenantConfig cfg = new TenantConfig();
                cfg.setTenant(savedTenant);
                cfg.setConfigKey(e.getKey());
                cfg.setConfigValue(e.getValue());
                tenantConfigRepository.save(cfg);
            }
        } else {
            TenantConfig def = new TenantConfig();
            def.setTenant(savedTenant);
            def.setConfigKey("payment_provider");
            def.setConfigValue("VNPAY");
            tenantConfigRepository.save(def);
        }

        // 6️⃣ Ghi log (RabbitMQ)
        publishSystemLog("CREATE_TENANT", savedTenant.getId(),
                "Tenant created with auto-generated manager account: " + username);

        return TenantResponse.fromEntity(savedTenant, managerUserId);
    }


    /**
     * Cập nhật trạng thái kích hoạt của tenant
     * @param tenantId
     * @param active
     * @return
     */
    @Transactional
    public TenantResponse updateTenantStatus(String tenantId, boolean active, String changedByUserId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // ✅ Lưu trạng thái cũ trước khi thay đổi
        TenantStatus oldStatus = tenant.getStatus();

        tenant.setStatus(active ? TenantStatus.ACTIVE : TenantStatus.INACTIVE);
        tenantRepository.save(tenant);

        // ✅ Gọi sang AuthService để cập nhật trạng thái tài khoản BQL
        ManagementAccount account = managementAccountRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Management account not found for tenant"));
        authClient.updateUserActiveStatus(account.getUserId(), active);

        // ✅ Ghi lịch sử thay đổi trạng thái
        TenantStatusHistory history = TenantStatusHistory.builder()
                .tenant(tenant)
                .oldStatus(oldStatus.name()) // ✅ bây giờ hợp lệ
                .newStatus(tenant.getStatus().name())
                .changedAt(Instant.now())
                .changedByUserId(changedByUserId)
                .note("Trạng thái tenant được cập nhật qua API quản lý")
                .build();
        tenantStatusHistoryRepository.save(history);

        publishSystemLog("UPDATE_TENANT_STATUS", tenant.getId(),
                "Tenant status changed to " + tenant.getStatus());

        return TenantResponse.fromEntity(tenant, account.getUserId());
    }


    // ========== CRUD cơ bản khác ==========
    public Tenant updateTenant(String id, Tenant updated) {
        Tenant existing = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setContactName(updated.getContactName());
        existing.setContactEmail(updated.getContactEmail());
        existing.setStatus(updated.getStatus());

        return tenantRepository.save(existing);
    }

    public void deleteTenant(String id) {
        tenantRepository.deleteById(id);
    }

    public Tenant getTenantById(String id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }


    // ========== Các phương thức hỗ trợ ==========
    private String generateUsernameBase(String fullName, String tenantName) {
        if (fullName == null) fullName = "bql";
        if (tenantName == null) tenantName = "tenant";

        String cleanName = fullName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String cleanTenant = tenantName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        // VD: Nguyễn Văn An - Sunrise Riverside -> nva.sunrise
        String[] parts = cleanName.split(" ");
        String initials = parts.length > 1
                ? ("" + parts[0].charAt(0) + parts[parts.length - 1]).toLowerCase()
                : cleanName.substring(0, Math.min(4, cleanName.length()));

        return initials + "." + cleanTenant.replaceAll(" ", "");
    }

    private String ensureUniqueUsername(String base) {
        String username = base;
        int counter = 1;
        while (true) {
            try {
                // Gọi Auth Service kiểm tra tồn tại
                boolean exists = authClient.checkUsernameExists(username);
                if (!exists) return username;
            } catch (Exception e) {
                return username; // fallback: nếu Auth chưa có API check
            }
            username = base + counter++;
        }
    }

    private void publishSystemLog(String action, String tenantId, String message) {
        try {
            Map<String, Object> log = Map.of(
                    "action", action,
                    "tenantId", tenantId,
                    "message", message,
                    "timestamp", Instant.now().toString()
            );

            String jsonLog = objectMapper.writeValueAsString(log);
            rabbitTemplate.convertAndSend("system.logs", "", jsonLog);

            System.out.println("[LOG] Published system log: " + jsonLog);
        } catch (Exception e) {
            System.err.println("[WARN] Failed to publish system log: " + e.getMessage());
        }
    }

}
