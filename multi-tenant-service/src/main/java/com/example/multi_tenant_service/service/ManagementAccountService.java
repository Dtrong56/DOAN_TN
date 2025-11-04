package com.example.multi_tenant_service.service;

import com.example.multi_tenant_service.dto.ManagementAccountResponse;
import com.example.multi_tenant_service.entity.ManagementAccount;
import com.example.multi_tenant_service.repository.ManagementAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagementAccountService {

    private final ManagementAccountRepository managementAccountRepository;

    public ManagementAccountResponse getTenantByUserId(UUID userId) {
        ManagementAccount account = managementAccountRepository.findFirstByUserId(userId.toString())
                .orElseThrow(() -> new RuntimeException("Management account not found for user " + userId));

        return ManagementAccountResponse.builder()
                .tenantId(account.getTenant().getId())
                .tenantName(account.getTenant().getName())
                .position(account.getProfile() != null ? account.getProfile().getPosition() : null)
                .fullName(account.getProfile() != null ? account.getProfile().getFullName() : null)
                .phone(account.getProfile() != null ? account.getProfile().getPhone() : null)
                .email(account.getProfile() != null ? account.getProfile().getEmail() : null)
                .build();
    }
}
