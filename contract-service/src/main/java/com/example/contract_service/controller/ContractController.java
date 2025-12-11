package com.example.contract_service.controller;

import com.example.contract_service.dto.ApproveAppendixRequest;
import com.example.contract_service.dto.ApproveAppendixResponse;
import com.example.contract_service.dto.ContractDto;
import com.example.contract_service.dto.ContractUploadRequest;
import com.example.contract_service.dto.MainContractResponse;
import com.example.contract_service.dto.RegisterAndSignAppendixRequest;
import com.example.contract_service.dto.RegisterAppendixResponse;
import com.example.contract_service.dto.ServiceAppendixResponse;
import com.example.contract_service.service.ContractService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;


    @Value("${file.storage.base-path}")
    private String basePath;


    //endpoint upload hợp đồng chung
    @PostMapping("/common")
    public ResponseEntity<MainContractResponse> uploadCommonContract(
            @RequestParam LocalDate signedDate,
            @RequestParam LocalDate effectiveDate,
            @RequestParam LocalDate expirationDate,
            @RequestParam Double monthlyFeePerM2,
            @RequestParam(required = false) String note,
            @RequestParam MultipartFile file
    ) throws Exception {

        ContractUploadRequest request = ContractUploadRequest.builder()
                .signedDate(signedDate)
                .effectiveDate(effectiveDate)
                .expirationDate(expirationDate)
                .monthlyFeePerM2(monthlyFeePerM2)
                .note(note)
                .file(file)
                .build();

        return ResponseEntity.ok(contractService.uploadCommonContract(request));
    }

    //endpoint download file hợp đồng chung
    @GetMapping("/common/{id}/file")
    public ResponseEntity<Resource> downloadContractFile(@PathVariable String id) throws IOException {
        Resource resource = contractService.loadContractFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    // Các endpoint lấy dữ liệu hợp đồng và phụ lục
    @GetMapping("/common")
    public ResponseEntity<List<MainContractResponse>> getAllMainContracts() {
        return ResponseEntity.ok(contractService.getAllMainContracts());
    }

    @GetMapping("/common/{id}")
    public ResponseEntity<MainContractResponse> getMainContractById(@PathVariable String id) {
        return ResponseEntity.ok(contractService.getMainContractById(id));
    }

    @GetMapping("/annex/by-resident")
    public ResponseEntity<List<ServiceAppendixResponse>> getAppendicesByResident() {
        return ResponseEntity.ok(contractService.getAppendicesByResident());
    }

    @GetMapping("/annex/{id}")
    public ResponseEntity<ServiceAppendixResponse> getAppendixById(@PathVariable String id) {
        return ResponseEntity.ok(contractService.getAppendixById(id));
    }

    //endpoint đăng ký và ký phụ lục hợp đồng
    @PostMapping("/appendix/register")
    public ResponseEntity<RegisterAppendixResponse> registerAppendix(
            @RequestBody RegisterAndSignAppendixRequest req) {
        return ResponseEntity.ok(contractService.registerAndSignAppendix(req));
    }

    //endpoint phê duyệt phụ lục hợp đồng
    @PostMapping("/appendix/approve")
    public ResponseEntity<ApproveAppendixResponse> approveAppendix(
            @RequestBody ApproveAppendixRequest request
    ) {
        ApproveAppendixResponse response = contractService.approveAppendix(request);
        return ResponseEntity.ok(response);
    }


    // Endpoint xem file PDF hợp đồng
   @GetMapping
    public ResponseEntity<?> getContracts() {
        List<ContractDto> contracts = contractService.getContractsForCurrentUser(); // service dùng TenantContext
        if (contracts.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Chưa có tài liệu liên quan"));
        }
        return ResponseEntity.ok(contracts);
    }


    // Endpoint xem file PDF hợp đồng
    @GetMapping("/{contractId}/pdf")
    public ResponseEntity<Resource> viewContractPdf(@PathVariable String contractId) throws IOException {
        Resource resource = contractService.getContractPdf(contractId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // Endpoint xem file PDF phụ lục hợp đồng
    @GetMapping("/appendices/{appendixId}/pdf")
    public ResponseEntity<Resource> viewAppendixPdf(@PathVariable String appendixId) throws IOException {
        Resource resource = contractService.getAppendixPdf(appendixId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
