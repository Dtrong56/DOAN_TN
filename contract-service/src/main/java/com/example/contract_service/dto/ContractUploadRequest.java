package com.example.contract_service.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractUploadRequest {
    private String contractCode;
    private LocalDate signedDate;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private Double monthlyFeePerM2;
    private String note;
    private MultipartFile file;
}
