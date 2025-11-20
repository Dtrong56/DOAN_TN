package com.example.contract_service.service;

import com.example.contract_service.client.AuthFeignClient;
import com.example.contract_service.client.CatalogClient;
import com.example.contract_service.client.ResidentClient;
import com.example.contract_service.dto.ApproveAppendixRequest;
import com.example.contract_service.dto.ApproveAppendixResponse;
import com.example.contract_service.dto.ContractDto;
// import com.example.contract_service.client.MonitoringClient;
import com.example.contract_service.dto.ContractUploadRequest;
import com.example.contract_service.dto.DigitalSignatureInternalDTO;
import com.example.contract_service.dto.MainContractResponse;
import com.example.contract_service.dto.PackageInfoDTO;
import com.example.contract_service.dto.RegisterAndSignAppendixRequest;
import com.example.contract_service.dto.RegisterAppendixResponse;
import com.example.contract_service.dto.ResidentInfoDTO;
import com.example.contract_service.dto.ServiceAppendixRequest;
import com.example.contract_service.dto.ServiceAppendixResponse;
import com.example.contract_service.dto.ServiceInfoDTO;
import com.example.contract_service.entity.AppendixHistory;
import com.example.contract_service.entity.AppendixStatus;
import com.example.contract_service.entity.MainContract;
import com.example.contract_service.entity.ServiceAppendix;
import com.example.contract_service.entity.SignatureRecord;
import com.example.contract_service.repository.MainContractRepository;
import com.example.contract_service.repository.ServiceAppendixRepository;
import com.example.contract_service.repository.SignatureRecordRepository;
import com.example.contract_service.repository.AppendixHistoryRepository;
import com.example.contract_service.security.TenantContext;
import com.example.contract_service.utils.RsaUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;




import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.nio.file.Path;



@Service
@RequiredArgsConstructor
public class ContractService {
    @Autowired
    private CatalogClient catalogClient;

    private final MainContractRepository mainContractRepository;
    private final TenantContext tenantContext;
    private final ServiceAppendixRepository serviceAppendixRepository;
    private final FileStorageService fileStorageService;
    private final SignatureRecordRepository signatureRecordRepository;
    private final ResidentClient residentClient;
    private final AppendixHistoryRepository appendixHistoryRepository;

    private final AuthFeignClient authClient;
    private final PdfGeneratorService pdfGenerator;
    
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
    
        /*
         * cư dân ký phụ lục dịch vụ
         */
        @Transactional
    public RegisterAppendixResponse registerAndSignAppendix(RegisterAndSignAppendixRequest req) {

        // 1. Lấy thông tin JWT
        String userId = tenantContext.getUserId();
        String tenantId = tenantContext.getTenantId();

        // 2. Lấy resident info
        ResidentInfoDTO resident = residentClient.getResidentByUserId(userId);
        String residentId = resident.getId();
        String apartmentId = resident.getApartmentId();

        // 3. Lấy thông tin service & package để validate
        ServiceInfoDTO serviceInfo = catalogClient.getServiceInfo(req.getServiceId());
        if (!serviceInfo.isActive()) {
                throw new RuntimeException("Service is inactive");
        }

        PackageInfoDTO packageInfo = catalogClient.getPackageOfService(req.getServiceId(), req.getPackageId());
        if (!packageInfo.isActive()) {
                throw new RuntimeException("Package is inactive");
        }

        // 4. Lấy main contract theo tenantId
        MainContract mainContract = mainContractRepository
                .findFirstByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant has no main contract"));

        // 5. Feign sang auth-service để lấy public key cư dân
        DigitalSignatureInternalDTO keyInfo = authClient.getDigitalSignature(userId);
        if (!keyInfo.isActive()) {
                throw new RuntimeException("User digital signature is inactive");
        }

        // 6. Verify chữ ký
        PublicKey publicKey = RsaUtils.loadPublicKey(keyInfo.getPublicKeyContent());

        boolean isValid = RsaUtils.verifyBase64(
                req.getSignedHash(),
                req.getSignatureValue(),
                publicKey,
                keyInfo.getAlgorithm()
        );

        if (!isValid) {
                throw new RuntimeException("Invalid resident signature");
        }

        // 7. Tính ngày hiệu lực
        LocalDate effectiveDate = LocalDate.now();
        LocalDate expirationDate = effectiveDate.plusMonths(packageInfo.getDurationMonths());

        // 8. Tạo appendix
        ServiceAppendix appendix = ServiceAppendix.builder()
                .mainContract(mainContract)
                .tenantId(tenantId)
                .serviceId(req.getServiceId())
                .packageId(req.getPackageId())
                .residentId(residentId)
                .apartmentId(apartmentId)
                .signedDate(LocalDate.now())
                .effectiveDate(effectiveDate)
                .expirationDate(expirationDate)
                .appendixStatus(AppendixStatus.PENDING_APPROVAL.name())
                .residentSignature(req.getSignatureValue())  // store signature
                .build();

        appendix = serviceAppendixRepository.save(appendix);

        // 9. Lưu signature record
        SignatureRecord record = SignatureRecord.builder()
                .serviceAppendix(appendix)
                .signerUserId(userId)
                .signerRole("RESIDENT")
                .signatureFilePath(null)
                .build();

        signatureRecordRepository.save(record);

        // 10. Trả response
        return RegisterAppendixResponse.builder()
                .appendixId(appendix.getId())
                .serviceId(req.getServiceId())
                .packageId(req.getPackageId())
                .residentId(residentId)
                .apartmentId(apartmentId)
                .effectiveDate(effectiveDate)
                .expirationDate(expirationDate)
                .status(AppendixStatus.PENDING_APPROVAL.name())
                .build();
        }

        /*
         * ban quản lý phê duyệt phụ lục dịch vụ
         */
        @Transactional
        public ApproveAppendixResponse approveAppendix(ApproveAppendixRequest req) {

                String approverId = tenantContext.getUserId();
                String tenantId = tenantContext.getTenantId();

                // 1. Load appendix
                ServiceAppendix appendix = serviceAppendixRepository.findById(req.getAppendixId())
                        .orElseThrow(() -> new RuntimeException("Appendix not found"));

                if (!appendix.getMainContract().getTenantId().equals(tenantId)) {
                throw new RuntimeException("You cannot approve appendix from another tenant");
                }

                // CASE 1: REJECT
                if ("REJECT".equalsIgnoreCase(req.getAction())) {

                if (req.getRejectReason() == null || req.getRejectReason().isBlank()) {
                        throw new RuntimeException("Reject reason is required");
                }

                appendix.setAppendixStatus(AppendixStatus.REJECTED.toString());
                serviceAppendixRepository.save(appendix);

                return ApproveAppendixResponse.builder()
                        .appendixId(appendix.getId())
                        .status("REJECTED")
                        .approvedDate(null)
                        .approverUserId(approverId)
                        .build();
                }

                // CASE 2: APPROVE
                if (!"APPROVE".equalsIgnoreCase(req.getAction())) {
                throw new RuntimeException("Invalid action, must be APPROVE or REJECT");
                }

                if (req.getSignedHash() == null || req.getSignatureValue() == null) {
                throw new RuntimeException("Missing digital signature");
                }

                // 2. Get manager public key from Auth-Service
                DigitalSignatureInternalDTO keyInfo = authClient.getDigitalSignature(approverId);
                if (!keyInfo.isActive()) {
                throw new RuntimeException("Digital signature is not active");
                }

                PublicKey publicKey = RsaUtils.loadPublicKey(keyInfo.getPublicKeyContent());
                boolean verified = RsaUtils.verifyBase64(
                        req.getSignedHash(),
                        req.getSignatureValue(),
                        publicKey,
                        keyInfo.getAlgorithm()
                );

                if (!verified) {
                throw new RuntimeException("Signature verification failed");
                }

                // 3. Lưu signature record của BQL
                SignatureRecord sig = signatureRecordRepository.save(
                        SignatureRecord.builder()
                                .serviceAppendix(appendix)
                                .signerUserId(approverId)
                                .signerRole("MANAGER")
                                .objectType("APPENDIX")
                                .objectId(appendix.getId())
                                .signedAt(LocalDateTime.now())
                                .signatureFilePath(req.getSignatureValue()) // base64
                                .build()
                );

                // 4. Lấy thông tin dịch vụ để in PDF
                ServiceInfoDTO serviceInfo = catalogClient.getServiceInfo(appendix.getServiceId());
                PackageInfoDTO packageInfo = catalogClient.getPackageOfService(
                        appendix.getServiceId(),
                        appendix.getPackageId()
                );

                String pdfBody = """
                        ===============================
                        APPROVED SERVICE APPENDIX
                        ===============================

                        Appendix ID: %s
                        Main Contract: %s

                        --- SERVICE INFO ---
                        Service: %s
                        Package: %s
                        Duration: %d months
                        Price: %.2f VND/month

                        --- BQL SIGNATURE ---
                        Approver: %s
                        Signed At: %s

                        Signed Hash:
                        %s

                        Signature Value (Base64):
                        %s
                        """.formatted(
                        appendix.getId(),
                        appendix.getMainContract().getContractCode(),
                        serviceInfo.getName(),
                        packageInfo.getName(),
                        packageInfo.getDurationMonths(),
                        packageInfo.getPrice(),
                        approverId,
                        sig.getSignedAt(),
                        req.getSignedHash(),
                        req.getSignatureValue()
                );

                byte[] pdfBytes = pdfBody.getBytes(StandardCharsets.UTF_8);

                // 5. Save PDF
                String pdfPath = fileStorageService.saveSignedAppendix(
                        tenantId,
                        appendix.getId(),
                        pdfBytes
                );

                appendix.setAppendixPdfPath(pdfPath);
                appendix.setAppendixStatus(AppendixStatus.APPROVED.toString());
                serviceAppendixRepository.save(appendix);

                // 6. Response
                return ApproveAppendixResponse.builder()
                        .appendixId(appendix.getId())
                        .status("ACTIVE")
                        .approvedDate(LocalDate.now())
                        .approverUserId(approverId)
                        .build();
        }

        // Endpoint xem file PDF hợp đồng và phụ lục cho cả cư dân và BQL
        private final String basePath; // từ @Value("${file.storage.base-path}")
        private final String serverBaseUrl; // từ @Value("${server.base-url}")
        
        //phuong thức lấy danh sách hợp đồng của user hiện tại
        public List<ContractDto> getContractsForCurrentUser() {
                String residentId = tenantContext.getResidentId();
                String tenantId = tenantContext.getTenantId();
                List<MainContract> contracts;

                if (residentId != null) { // Cư dân
                        contracts = mainContractRepository.findByResidentId(residentId);
                } else if (tenantId != null) { // BQL
                        contracts = mainContractRepository.findByTenantId(tenantId);
                } else {
                        throw new AccessDeniedException("Không có quyền truy cập");
                }

                List<ContractDto> dtoList = new ArrayList<>();
                        for (MainContract contract : contracts) {
                        List<ServiceAppendix> appendices;
                        if (residentId != null) {
                                appendices = serviceAppendixRepository.findByMainContractIdAndResidentId(contract.getId(), residentId);
                        } else {
                                appendices = serviceAppendixRepository.findByMainContract_Id(contract.getId());
                        }
                        ContractDto dto = ContractDto.from(contract, appendices, serverBaseUrl);
                        dtoList.add(dto);
                }

                return dtoList; // thêm return
        }


        //Phương thức lấy file PDF hợp đồng hoặc phụ lục
        public Resource getContractPdf(String contractId) throws IOException {
                MainContract contract = mainContractRepository.findById(contractId)
                        .orElseThrow(() -> new RuntimeException("Contract not found"));
                Path filePath = Paths.get(basePath).resolve(contract.getPdfFilePath());
                if (!Files.exists(filePath)) throw new RuntimeException("PDF file not found");
                return new UrlResource(filePath.toUri());
        }

        //Phương thức lấy file PDF phụ lục
        public Resource getAppendixPdf(String appendixId) throws IOException {
                ServiceAppendix appendix = serviceAppendixRepository.findById(appendixId)
                        .orElseThrow(() -> new RuntimeException("Appendix not found"));

                if (appendix.getAppendixPdfPath() == null) {
                        throw new RuntimeException("Appendix is not approved or has no PDF");
                }

                Path filePath = Paths.get(appendix.getAppendixPdfPath()); // <-- IMPORTANT: absolute path

                if (!Files.exists(filePath)) {
                        throw new RuntimeException("PDF file not found");
                }

                return new UrlResource(filePath.toUri());
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
