package com.example.contract_service.dto;

import lombok.Data;

import lombok.Builder;
import java.time.LocalDate;

@Data
@Builder
public class RegisterAppendixResponse {
    private String appendixId;
    private String serviceId;
    private String packageId;
    private String residentId;
    private String apartmentId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String status; // PENDING_USER_SIGN
}


