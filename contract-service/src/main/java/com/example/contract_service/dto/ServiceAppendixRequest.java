package com.example.contract_service.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAppendixRequest {
    private String mainContractId;
    private String serviceId;
    private String packageId;
    private String apartmentId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String note;
}
