package com.example.contract_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AppendixDto {
    private String appendixId;
    private String serviceId;
    private String packageId;
    private String residentId;
    private String apartmentId;
    private LocalDate signedDate;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String appendixPdfUrl;
    private String appendixStatus;
}
