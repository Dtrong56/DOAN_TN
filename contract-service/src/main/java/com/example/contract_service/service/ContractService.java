package com.example.contract_service.service;

// import com.example.contract_service.client.MonitoringClient;
import com.example.contract_service.dto.ContractUploadRequest;
import com.example.contract_service.dto.MainContractResponse;
import com.example.contract_service.dto.ServiceAppendixRequest;
import com.example.contract_service.dto.ServiceAppendixResponse;
import com.example.contract_service.entity.MainContract;
import com.example.contract_service.entity.ServiceAppendix;
import com.example.contract_service.repository.MainContractRepository;
import com.example.contract_service.repository.ServiceAppendixRepository;
import com.example.contract_service.security.TenantContext;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final MainContractRepository mainContractRepository;
    private final TenantContext tenantContext;
    private final ServiceAppendixRepository serviceAppendixRepository;
    private final FileStorageService fileStorageService;
    // private final MonitoringClient monitoringClient;


    /*
     * upload hợp đồng chung
     */
    @Transactional
    public MainContractResponse uploadCommonContract(ContractUploadRequest req) throws IOException {
        String tenantId = tenantContext.getTenantId();
        String userId = tenantContext.getUserId();

        // Tạo mã hợp đồng tự động
        String nextCode = generateContractCode(tenantId);
        req.setContractCode(nextCode);

        if (tenantId == null) throw new RuntimeException("Missing tenant context");

        // ✅ Lưu file PDF thông qua FileStorageService
        String pdfPath = fileStorageService.saveContractFile(tenantId, req.getFile(), req.getContractCode());

        // ✅ Lưu DB
        MainContract contract = MainContract.builder()
                .tenantId(tenantId)
                .contractCode(req.getContractCode())
                .signedDate(req.getSignedDate())
                .effectiveDate(req.getEffectiveDate())
                .expirationDate(req.getExpirationDate())
                .pdfFilePath(pdfPath)
                .build();

        mainContractRepository.save(contract);

        // (Tạm comment log monitoring)
        // Map<String, Object> log = new HashMap<>();
        // log.put("userId", userId);
        // log.put("tenantId", tenantId);
        // log.put("action", "CREATE_CONTRACT");
        // log.put("objectType", "MainContract");
        // log.put("objectId", contract.getId());
        // log.put("description", "Upload hợp đồng vận hành chung");
        // monitoringClient.createLog(log);

        return MainContractResponse.builder()
                .id(contract.getId())
                .tenantId(contract.getTenantId())
                .contractCode(contract.getContractCode())
                .signedDate(contract.getSignedDate())
                .effectiveDate(contract.getEffectiveDate())
                .expirationDate(contract.getExpirationDate())
                .pdfFilePath(contract.getPdfFilePath())
                .build();
    }

    /*
     * tải file hợp đồng
     */
    public Resource loadContractFile(String contractId) {
        String tenantId = tenantContext.getTenantId();
        MainContract contract = mainContractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));

        // 🔒 Kiểm tra quyền tenant
        if (!contract.getTenantId().equals(tenantId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        File file = new File(contract.getPdfFilePath());
        if (!file.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on server");
        }

        return new FileSystemResource(file);
        }
    /*
     * đăng ký phụ lục dịch vụ
     */
    @Transactional
    public ServiceAppendixResponse registerServiceAppendix(ServiceAppendixRequest req) {
        String tenantId = tenantContext.getTenantId();
        String residentId = tenantContext.getUserId();

        if (tenantId == null || residentId == null)
            throw new RuntimeException("Missing tenant or user context");

        // Kiểm tra hợp đồng nền còn hiệu lực
        MainContract mainContract = mainContractRepository.findById(req.getMainContractId())
                .orElseThrow(() -> new RuntimeException("Main contract not found"));

        LocalDate today = LocalDate.now();
        if (mainContract.getEffectiveDate().isAfter(today) || mainContract.getExpirationDate().isBefore(today))
            throw new RuntimeException("Main contract not in effect");

        // Kiểm tra phụ lục trùng lặp
        boolean exists = serviceAppendixRepository.findByMainContract_Id(req.getMainContractId())
                .stream()
                .anyMatch(a -> a.getServiceId().equals(req.getServiceId())
                        && a.getApartmentId().equals(req.getApartmentId())
                        && a.getExpirationDate().isAfter(today));

        if (exists)
            throw new RuntimeException("Service already registered for this apartment");

        // Lưu phụ lục mới
        ServiceAppendix appendix = ServiceAppendix.builder()
                .mainContract(mainContract)
                .serviceId(req.getServiceId())
                .packageId(req.getPackageId())
                .residentId(residentId)
                .apartmentId(req.getApartmentId())
                .effectiveDate(req.getEffectiveDate())
                .expirationDate(req.getExpirationDate())
                .build();

        serviceAppendixRepository.save(appendix);

        // Ghi log Monitoring (tạm comment)
        // Map<String, Object> log = new HashMap<>();
        // log.put("userId", residentId);
        // log.put("tenantId", tenantId);
        // log.put("action", "CREATE_APPENDIX");
        // log.put("objectType", "ServiceAppendix");
        // log.put("objectId", appendix.getId());
        // log.put("description", "Cư dân đăng ký dịch vụ tiện ích");
        // monitoringClient.createLog(log);

        return ServiceAppendixResponse.builder()
                .id(appendix.getId())
                .mainContractId(mainContract.getId())
                .serviceId(appendix.getServiceId())
                .packageId(appendix.getPackageId())
                .residentId(appendix.getResidentId())
                .apartmentId(appendix.getApartmentId())
                .effectiveDate(appendix.getEffectiveDate())
                .expirationDate(appendix.getExpirationDate())
                .status("PENDING_APPROVAL")
                .build();
    }


    // Các phương thức lấy dữ liệu hợp đồng và phụ lục
    public List<MainContractResponse> getAllMainContracts() {
        String tenantId = tenantContext.getTenantId();
        return mainContractRepository.findByTenantId(tenantId).stream()
                .map(c -> MainContractResponse.builder()
                        .id(c.getId())
                        .tenantId(c.getTenantId())
                        .contractCode(c.getContractCode())
                        .signedDate(c.getSignedDate())
                        .effectiveDate(c.getEffectiveDate())
                        .expirationDate(c.getExpirationDate())
                        .pdfFilePath(c.getPdfFilePath())
                        .build())
                .toList();
    }

    public MainContractResponse getMainContractById(String id) {
        MainContract c = mainContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        return MainContractResponse.builder()
                .id(c.getId())
                .tenantId(c.getTenantId())
                .contractCode(c.getContractCode())
                .signedDate(c.getSignedDate())
                .effectiveDate(c.getEffectiveDate())
                .expirationDate(c.getExpirationDate())
                .pdfFilePath(c.getPdfFilePath())
                .build();
    }

    public List<ServiceAppendixResponse> getAppendicesByResident() {
        String residentId = tenantContext.getUserId();
        String tenantId = tenantContext.getTenantId();
        return serviceAppendixRepository.findAll().stream()
                .filter(a -> a.getResidentId().equals(residentId))
                .map(a -> ServiceAppendixResponse.builder()
                        .id(a.getId())
                        .mainContractId(a.getMainContract().getId())
                        .serviceId(a.getServiceId())
                        .packageId(a.getPackageId())
                        .residentId(a.getResidentId())
                        .apartmentId(a.getApartmentId())
                        .effectiveDate(a.getEffectiveDate())
                        .expirationDate(a.getExpirationDate())
                        .status("ACTIVE") // hoặc xác định theo ngày
                        .build())
                .toList();
    }

    public ServiceAppendixResponse getAppendixById(String id) {
        ServiceAppendix a = serviceAppendixRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appendix not found"));
        return ServiceAppendixResponse.builder()
                .id(a.getId())
                .mainContractId(a.getMainContract().getId())
                .serviceId(a.getServiceId())
                .packageId(a.getPackageId())
                .residentId(a.getResidentId())
                .apartmentId(a.getApartmentId())
                .effectiveDate(a.getEffectiveDate())
                .expirationDate(a.getExpirationDate())
                .status("ACTIVE")
                .build();
    }

    // Phương thức hỗ trợ tạo mã hợp đồng
    private String generateContractCode(String tenantId) {
        String year = String.valueOf(LocalDate.now().getYear());
        long nextSeq = mainContractRepository.countByTenantId(tenantId) + 1;

        String contractCode;
        do {
                contractCode = String.format("HD-%s-%s-%03d", tenantId, year, nextSeq);
                nextSeq++;
        } while (mainContractRepository.existsByContractCode(contractCode));

        return contractCode;
        }
}
