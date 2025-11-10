package com.example.resident_service.service;

import com.example.resident_service.client.AuthFeignClient;
import com.example.resident_service.dto.*;
import com.example.resident_service.entity.*;
import com.example.resident_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.resident_service.security.TenantContext;

import java.time.LocalDate;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final ApartmentRepository apartmentRepository;
    private final ApartmentOwnershipRepository ownershipRepository;
    private final OwnershipTransferHistoryRepository historyRepository;
    private final ResidentProfileRepository profileRepository;
    private final ResidentAccountRepository accountRepository;
    private final AuthFeignClient authFeignClient;
    private final TenantContext tenantContext;

    @Transactional
    public OwnershipTransferResult transferOwnership(OwnershipTransferRequest req) {

        String tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            return new OwnershipTransferResult(false, "Missing tenantId in JWT", null, null, null
            , null, null, null, null);
        }

        // 1) Kiem tra apartment ton tai
        Apartment apt = apartmentRepository.findById(req.getApartmentId())
                .orElseThrow(() -> new RuntimeException("Apartment not found"));

        // 2) tìm hoặc tạo ResidentProfile
        ResidentProfile profile = profileRepository.findByCccd(req.getCccd())
                .orElseGet(() -> {
                    ResidentProfile p = ResidentProfile.builder()
                            .fullName(req.getFullName())
                            .cccd(req.getCccd())
                            .phone(req.getPhone())
                            .email(req.getEmail())
                            .dateOfBirth(req.getDateOfBirth())
                            .build();
                    return profileRepository.save(p);
                });

        // 3) Chắc chắn có ResidentAccount trong tenant
        // Tìm userId của cư dân cũ nếu có
                String oldUserId = null;
                ApartmentOwnership oldOwner = ownershipRepository.findActiveOwnership(req.getApartmentId(), tenantId).orElse(null);
                if (oldOwner != null) {
                oldUserId = accountRepository
                        .findByTenantIdAndResidentProfileId(tenantId, oldOwner.getResidentId())
                        .map(ResidentAccount::getUserId)
                        .orElse(null);
                }

                // Gọi auth-service để vô hiệu hóa user cũ và tạo user mới
                AuthTransferUserRequest transferReq = new AuthTransferUserRequest(
                        oldUserId,
                        profile.getFullName(),
                        profile.getCccd(),
                        profile.getEmail(),
                        profile.getPhone(),
                        tenantId
                );

                AuthCreateResult result = authFeignClient.transferUser(transferReq);
                if (result.getUserId() == null) {
                throw new RuntimeException("Auth-service failed: " + result.getError());
                }

                // Tạo ResidentAccount mới
                ResidentAccount newAcc = ResidentAccount.builder()
                        .tenantId(tenantId)
                        .userId(result.getUserId())
                        .residentProfile(profile)
                        .status(ResidentAccountStatus.ACTIVE)
                        .build();

                ResidentAccount account = accountRepository.save(newAcc);
        // 4) Kết thúc ownership hiện tại (nếu có)
        LocalDate transferDate = req.getTransferDate() != null ? req.getTransferDate() : LocalDate.now();
        if (oldOwner != null) {
            oldOwner.setEndDate(transferDate.minusDays(1));
            ownershipRepository.save(oldOwner);
        }

        // 5) Tạo ownership mới
        ApartmentOwnership newOwner = ApartmentOwnership.builder()
                .apartment(apt)
                .residentId(profile.getId()) // Gán đúng residentId từ profile
                .startDate(transferDate)
                .build();
        newOwner = ownershipRepository.save(newOwner);

        // 6) Luu lịch sử chuyển nhượng
        OwnershipTransferHistory history = OwnershipTransferHistory.builder()
                .apartmentId(apt.getId()) // Lấy ID từ Apartment
                .fromResidentId(oldOwner != null ? oldOwner.getResidentId() : null) // Lấy residentId từ oldOwner
                .toResidentId(profile.getId()) // Lấy ID từ ResidentProfile mới
                .transferDate(transferDate)
                .note(req.getNote())
                .build();
        historyRepository.save(history);

        // 7) Trả về kết quả
        // Lấy thông tin cũ nếu có
        ResidentProfile oldProfile = null;
        if (oldOwner != null) {
                oldProfile = profileRepository.findById(oldOwner.getResidentId()).orElse(null);
        }
        // Trả về kết quả
        return OwnershipTransferResult.builder()
                .success(true)
                .message("Ownership transferred")
                .apartmentId(apt.getId())
                .apartmentCode(apt.getCode())
                .oldOwnerFullName(oldProfile != null ? oldProfile.getFullName() : null)
                .oldOwnerCccd(oldProfile != null ? oldProfile.getCccd() : null)
                .newOwnerFullName(profile.getFullName())
                .newOwnerCccd(profile.getCccd())
                .newOwnerUserId(account.getUserId())
                .build();
    }
}
