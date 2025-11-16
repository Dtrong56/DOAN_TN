package com.example.resident_service.service;

import com.example.resident_service.dto.ResidentAccountInternalResponse;
import com.example.resident_service.dto.ResidentAccountResponse;
import com.example.resident_service.entity.Apartment;
import com.example.resident_service.entity.ApartmentOwnership;
import com.example.resident_service.entity.ResidentAccount;
import com.example.resident_service.repository.ResidentAccountRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentAccountRepository residentAccountRepository;
    private final com.example.resident_service.repository.ResidentAccountRepository accountRepo;
    private final com.example.resident_service.repository.ApartmentOwnershipRepository ownershipRepo;
    private final com.example.resident_service.repository.ApartmentRepository apartmentRepo;

    /*
     * TÌm kiếm tài khoản cư dân dựa trên userId
     */
    public ResidentAccountResponse getTenantByUser(String userId) {
        ResidentAccount account = residentAccountRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Resident account not found for userId: " + userId));

        return ResidentAccountResponse.builder()
                .tenantId(account.getTenantId())
                .residentProfileId(account.getResidentProfile().getId())
                .build();
    }

    /*
     * Tìm kiếm thông tin cư dân nội bộ dựa trên userId
     */
    public ResidentAccountInternalResponse getResidentInfoByUserId(String userId) {

        // 1. tìm account theo userId
        ResidentAccount acc = accountRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Resident account not found for userId=" + userId));

        String residentId = acc.getResidentProfile().getId();

        // 2. tìm căn hộ cư dân đang sở hữu
        ApartmentOwnership ownership = ownershipRepo.findActiveOwnershipByResidentId(residentId)
                .orElseThrow(() -> new RuntimeException("Resident has no active apartment"));

        Apartment apt = apartmentRepo.findById(ownership.getApartment().getId())
                .orElseThrow(() -> new RuntimeException("Apartment not found"));

        return new ResidentAccountInternalResponse(
                residentId,
                apt.getId(),
                apt.getBuilding().getId()
        );
    }
}


