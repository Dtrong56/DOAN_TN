package com.example.resident_service.service;

import com.example.resident_service.dto.ResidentAccountResponse;
import com.example.resident_service.entity.ResidentAccount;
import com.example.resident_service.repository.ResidentAccountRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentAccountRepository residentAccountRepository;

    public ResidentAccountResponse getTenantByUser(String userId) {
        ResidentAccount account = residentAccountRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Resident account not found for userId: " + userId));

        return ResidentAccountResponse.builder()
                .tenantId(account.getTenantId())
                .residentProfileId(account.getResidentProfile().getId())
                .build();
    }
}


