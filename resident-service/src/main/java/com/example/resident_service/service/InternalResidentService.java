package com.example.resident_service.service;

import com.example.resident_service.dto.ApartmentDTO;
import com.example.resident_service.dto.ResidentDTO;
import com.example.resident_service.entity.ApartmentOwnership;
import com.example.resident_service.entity.ResidentAccount;
import com.example.resident_service.entity.ResidentAccountStatus;
import com.example.resident_service.repository.ApartmentOwnershipRepository;
import com.example.resident_service.repository.ResidentAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InternalResidentService {

    private final ResidentAccountRepository accountRepo;
    private final ApartmentOwnershipRepository ownershipRepo;

    public List<ResidentDTO> getResidents(String tenantId, boolean activeOnly, boolean includeApartment) {

        List<ResidentAccount> accounts = activeOnly
                ? accountRepo.findByTenantIdAndStatus(tenantId, ResidentAccountStatus.ACTIVE)
                : accountRepo.findByTenantId(tenantId);

        return accounts.stream().map(acc -> {
            var profile = acc.getResidentProfile();

            ResidentDTO dto = new ResidentDTO();
            dto.setResidentId(acc.getId());
            dto.setFullName(profile.getFullName());
            dto.setEmail(profile.getEmail());
            dto.setPhone(profile.getPhone());

            if (includeApartment) {
                ApartmentDTO apm = loadApartment(acc.getId());
                dto.setApartment(apm);
            }

            return dto;
        }).toList();
    }

    private ApartmentDTO loadApartment(String residentId) {

        List<ApartmentOwnership> ownerships =
                ownershipRepo.findByResidentIdAndIsRepresentativeTrue(residentId);

        if (ownerships.isEmpty()) return null;

        var own = ownerships.get(0);
        var ap = own.getApartment();

        ApartmentDTO dto = new ApartmentDTO();
        dto.setApartmentId(ap.getId());
        dto.setApartmentCode(ap.getCode());
        dto.setArea(ap.getAreaM2());
        return dto;
    }
}
