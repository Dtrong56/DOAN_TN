package com.example.resident_service.controller;

import com.example.resident_service.dto.ImportConfirmRequest;
import com.example.resident_service.dto.ImportPreviewResponse;
import com.example.resident_service.dto.ImportResultResponse;
import com.example.resident_service.service.ApartmentImportService;
import com.example.resident_service.service.OwnershipService;
import com.example.resident_service.dto.OwnershipTransferRequest;
import com.example.resident_service.dto.OwnershipTransferResult;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/resident/import")
@RequiredArgsConstructor
public class ResidentController {

    private final ApartmentImportService importService;

    private final OwnershipService ownershipService;

    // Endpoint to preview the import file
    @PostMapping(path = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportPreviewResponse> preview(
            @RequestParam("file") MultipartFile file) throws Exception {
        ImportPreviewResponse resp = importService.preview(file);
        return ResponseEntity.ok(resp);
    }

    // Endpoint to confirm and execute the import
    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportResultResponse> confirm(@RequestBody ImportConfirmRequest request) {
        return ResponseEntity.ok(importService.confirmBulk(request));
    }

    // Endpoint to handle ownership transfer
    @PostMapping("/transfer")
    public ResponseEntity<OwnershipTransferResult> transfer(@RequestBody OwnershipTransferRequest req) {
        return ResponseEntity.ok(ownershipService.transferOwnership(req));
    }
}

