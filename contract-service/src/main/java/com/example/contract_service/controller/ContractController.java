package com.example.contract_service.controller;

import com.example.contract_service.dto.ContractUploadRequest;
import com.example.contract_service.dto.MainContractResponse;
import com.example.contract_service.dto.ServiceAppendixRequest;
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

@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

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


    //endpoint đăng ký phụ lục dịch vụ
    @PostMapping("/annex")
    public ResponseEntity<ServiceAppendixResponse> registerServiceAppendix(
            @RequestBody ServiceAppendixRequest request
    ) {
        return ResponseEntity.status(201)
                .body(contractService.registerServiceAppendix(request));
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

}
