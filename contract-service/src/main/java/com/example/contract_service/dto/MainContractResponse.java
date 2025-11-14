package com.example.contract_service.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainContractResponse {
    private String id;
    private String tenantId;
    private String contractCode;
    private LocalDate signedDate;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String pdfFilePath;
}
