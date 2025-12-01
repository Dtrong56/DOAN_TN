package com.example.contract_service.service;

import com.example.contract_service.client.AuthFeignClient;
import com.example.contract_service.client.CatalogClient;
import com.example.contract_service.client.ResidentClient;
import com.example.contract_service.dto.ApproveAppendixRequest;
import com.example.contract_service.dto.ApproveAppendixResponse;
import com.example.contract_service.dto.ContractDto;
import com.example.contract_service.client.MonitoringClient;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;



@Service
@RequiredArgsConstructor
public class ContractService {
    @Autowired
    private CatalogClient catalogClient;
    @Autowired
    private ResidentClient residentClient;
    @Autowired
    private AuthFeignClient authClient;

    @Value("${server.base-url}")
    private String serverBaseUrl;

    private final MainContractRepository mainContractRepository;
    private final TenantContext tenantContext;
    private final ServiceAppendixRepository serviceAppendixRepository;
    private final AppendixHistoryRepository appendixHistoryRepository;
    private final FileStorageService fileStorageService;
    private final SignatureRecordRepository signatureRecordRepository;
    private final MonitoringClient monitoringClient;


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

        // Gọi MonitoringClient ghi log lịch sử upload hợp đồng 
        monitoringClient.createLog(
                new com.example.contract_service.dto.SystemLogDTO(
                        LocalDateTime.now(),
                        userId,
                        tenantId,
                        "ADMIN",
                        "UPLOAD_CONTRACT",
                        "Contract",
                        contract.getId(),
                        "Uploaded new common contract with code " + contract.getContractCode(),
                        null,
                        "ContractService",
                        "/contracts/upload",
                        null
                )
        );

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
        String residentId = tenantContext.getResidentId();
        System.out.println("ResidentID: "+userId);

        // 2. Lấy resident info
        ResidentInfoDTO resident = residentClient.getResidentByUserId(userId);
        // String residentId = resident.getId();
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

        // 6. Verify chữ ký cư dân
        PublicKey publicKey = RsaUtils.loadPublicKey(keyInfo.getPublicKeyContent());

        // rawContent phải giống file data.txt mà bạn ký bằng OpenSSL
        String rawContent = String.format(
                "{\"serviceId\":\"%s\",\"packageId\":\"%s\"}",
                req.getServiceId(),
                req.getPackageId()
        );

        boolean isValid = RsaUtils.verifySignatureRaw(
                rawContent,
                req.getSignatureValue(),
                publicKey,
                keyInfo.getAlgorithm()
        );

        // System.out.println("Raw content bytes: " + Arrays.toString(rawContent.getBytes(StandardCharsets.UTF_8)));
        // System.out.println("Signature bytes length: " + req.getSignatureValue().length());
        // System.out.println("Public key: " + publicKey);

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
                .objectType("APPENDIX")
                .objectId(appendix.getId())
                .signerRole("RESIDENT")
                .signedAt(LocalDateTime.now())
                .signatureFilePath(null)
                .build();

        signatureRecordRepository.save(record);

        // 10. Tạo bản lịch sử đầu tiên (version 1)
        createHistory(
                appendix,
                "REGISTERED",
                null, // UC07 chưa tạo PDF
                "Resident registered and signed appendix"
        );

        // 11.Trả response
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
                System.out.println("Step 1: Load appendix");
                ServiceAppendix appendix = serviceAppendixRepository.findById(req.getAppendixId())
                        .orElseThrow(() -> new RuntimeException("Appendix not found"));
                
                System.out.println("TenantID từ jwt: "+tenantId);
                System.out.println("TenantID từ appendix: "+appendix.getMainContract().getTenantId());

                // Kiểm tra quyền
                if (!appendix.getMainContract().getTenantId().equals(tenantId)) {
                        throw new RuntimeException("You cannot approve appendix from another tenant");
                }

                System.out.println("Chọn 2 case!");

                // CASE 1: REJECT
                System.out.println("Case 1: REJECT!");
                if ("REJECT".equalsIgnoreCase(req.getAction())) {

                        if (req.getRejectReason() == null || req.getRejectReason().isBlank()) {
                        throw new RuntimeException("Reject reason is required");
                        }

                        appendix.setAppendixStatus(AppendixStatus.REJECTED.name());
                        serviceAppendixRepository.save(appendix);

                        return ApproveAppendixResponse.builder()
                                .appendixId(appendix.getId())
                                .status("REJECTED")
                                .approvedDate(null)
                                .approverUserId(approverId)
                                .build();
                }

                //Feign call tới monitoring-service để ghi log từ chối phụ lục
                monitoringClient.createLog(
                        new com.example.contract_service.dto.SystemLogDTO(
                                LocalDateTime.now(),
                                approverId,
                                tenantId,
                                "MANAGER",
                                "REJECT_APPENDIX",
                                "ServiceAppendix",
                                appendix.getId(),
                                "Rejected service appendix with reason: " + req.getRejectReason(),
                                null,
                                "ContractService",
                                "/appendices/approve",
                                null
                        )
                );

                // CASE 2: APPROVE
                System.out.println("Case 2: APPROVE!");
                if (!"APPROVE".equalsIgnoreCase(req.getAction())) {
                        throw new RuntimeException("Invalid action, must be APPROVE or REJECT");
                }

                if (req.getSignatureValue() == null) {
                        throw new RuntimeException("Missing digital signature");
                }

                // 2. Lấy public key BQL từ Auth
                System.out.println("case 2 - step 2");
                DigitalSignatureInternalDTO keyInfo = authClient.getDigitalSignature(approverId);
                if (!keyInfo.isActive()) {
                        throw new RuntimeException("Digital signature is not active");
                }

                PublicKey publicKey = RsaUtils.loadPublicKey(keyInfo.getPublicKeyContent());

                // 3. Raw JSON để verify (y như file data.txt trong UC07)
                System.out.println("case 2 - step 3");
                String rawContent = String.format(
                        "{\"appendixId\":\"%s\",\"action\":\"%s\"}",
                        req.getAppendixId(),
                        req.getAction()
                );

                boolean verified = RsaUtils.verifySignatureRaw(
                        rawContent,
                        req.getSignatureValue(),
                        publicKey,
                        keyInfo.getAlgorithm()
                );

                if (!verified) {
                        throw new RuntimeException("Signature verification failed");
                }

                // 4. Lưu manager signature record
                System.out.println("case 2 - step 4");
                SignatureRecord sig = signatureRecordRepository.save(
                        SignatureRecord.builder()
                                .serviceAppendix(appendix)
                                .signerUserId(approverId)
                                .signerRole("MANAGER")
                                .objectType("APPENDIX")
                                .objectId(appendix.getId())
                                .signedAt(LocalDateTime.now())
                                .signatureFilePath(req.getSignatureValue()) 
                                .build()
                );

                // 5. Lấy thông tin dịch vụ để in PDF
                System.out.println("case 2 - step 5");
                ServiceInfoDTO serviceInfo = catalogClient.getServiceInfo(appendix.getServiceId());
                PackageInfoDTO packageInfo = catalogClient.getPackageOfService(
                        appendix.getServiceId(),
                        appendix.getPackageId()
                );
                System.out.println("Lấy thông tin dịch vụ xong!");
                System.out.println("Tạo nội dung PDF...");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Document document = new Document();

                try {
                PdfWriter.getInstance(document, baos);

                document.open();
                document.add(new Paragraph("APPROVED SERVICE APPENDIX"));
                document.add(new Paragraph("Appendix ID: " + appendix.getId()));
                document.add(new Paragraph("Main Contract: " + appendix.getMainContract().getContractCode()));
                document.add(new Paragraph("--- SERVICE INFO ---"));
                document.add(new Paragraph("Service: " + serviceInfo.getName()));
                document.add(new Paragraph("Package: " + packageInfo.getName()));
                document.add(new Paragraph("Duration: " + packageInfo.getDurationMonths() + " months"));
                document.add(new Paragraph("Price: " + packageInfo.getPrice() + " VND/month"));
                document.add(new Paragraph("--- BQL SIGNATURE ---"));
                document.add(new Paragraph("Approver: " + approverId));
                document.add(new Paragraph("Signed At: " + sig.getSignedAt()));
                document.add(new Paragraph("Raw Content: " + rawContent));
                document.add(new Paragraph("Signature Value (Base64): " + req.getSignatureValue()));
                } catch (Exception e) {
                // log lỗi hoặc throw RuntimeException để service xử lý
                throw new RuntimeException("Error generating appendix PDF: " + e.getMessage(), e);
                } finally {
                document.close();
                }

                byte[] pdfBytes = baos.toByteArray();
                String pdfPath = fileStorageService.saveSignedAppendix(tenantId, appendix.getId(), pdfBytes);

                // 7. Cập nhật trạng thái phụ lục
                System.out.println("case 2 - step 7");
                appendix.setAppendixPdfPath(pdfPath);
                appendix.setAppendixStatus(AppendixStatus.APPROVED.name());
                serviceAppendixRepository.save(appendix);

                System.out.println("case 2 - DONE");

                //8. Feign call tới monitoring-service để ghi log phê duyệt phụ lục
                monitoringClient.createLog(
                        new com.example.contract_service.dto.SystemLogDTO(
                                LocalDateTime.now(),
                                approverId,
                                tenantId,
                                "MANAGER",
                                "APPROVE_APPENDIX",
                                "ServiceAppendix",
                                appendix.getId(),
                                "Approved service appendix.",
                                null,
                                "ContractService",
                                "/appendices/approve",
                                null
                        )
                );

                // 9. Ghi history (version 2)
                createHistory(
                        appendix,
                        "APPROVED",
                        pdfPath,
                        "Manager approved appendix"
                );

                return ApproveAppendixResponse.builder()
                        .appendixId(appendix.getId())
                        .status("ACTIVE")
                        .approvedDate(LocalDate.now())
                        .approverUserId(approverId)
                        .build();
        }


        
        
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
                        // Tạo DTO hợp đồng kèm phụ lục
                        //serverBaseUrl lấy từ application.properties để tạo link download
                        ContractDto dto = ContractDto.from(contract, appendices, serverBaseUrl);
                        dtoList.add(dto);
                }

                return dtoList; // thêm return
        }


        //Phương thức lấy file PDF hợp đồng hoặc phụ lục
        public Resource getContractPdf(String contractId) throws IOException {
                MainContract contract = mainContractRepository.findById(contractId)
                        .orElseThrow(() -> new RuntimeException("Contract not found"));
                Path filePath = Paths.get(contract.getPdfFilePath());
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

        /**
         * Tạo lịch sử thay đổi phụ lục
         * @param appendix
         * @param changeType
         * @param pdfPath
         * @param note
         */
        @Transactional
    public void createHistory(
            ServiceAppendix appendix,
            String changeType,
            String pdfPath,
            String note
    ) {
        // Lấy version hiện tại
        long count = appendixHistoryRepository
                .countByServiceAppendixId(appendix.getId());

        int nextVersion = (int) count + 1;

        AppendixHistory history = AppendixHistory.builder()
                .serviceAppendix(appendix)
                .versionNo(nextVersion)
                .changeType(changeType)
                .changedByUserId(tenantContext.getUserId())
                .changedAt(LocalDateTime.now())

                // Old/new — lần đầu đăng ký thì để null các old
                .oldEffectiveDate(null)
                .newEffectiveDate(appendix.getEffectiveDate())

                .oldExpirationDate(null)
                .newExpirationDate(appendix.getExpirationDate())

                .oldPackageId(null)
                .newPackageId(appendix.getPackageId())

                .note(note)
                .pdfPath(pdfPath)

                .build();

        appendixHistoryRepository.save(history);
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
