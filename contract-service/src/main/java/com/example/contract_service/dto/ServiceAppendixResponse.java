package com.example.contract_service.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAppendixResponse {
    private String id;
    private String mainContractId;
    private String serviceId;
    private String packageId;
    private String residentId;
    private String apartmentId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String status;
}
