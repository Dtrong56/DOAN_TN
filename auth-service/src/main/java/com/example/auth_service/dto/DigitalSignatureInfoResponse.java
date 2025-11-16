package com.example.auth_service.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DigitalSignatureInfoResponse {

    private String userId;
    private String fullName;
    private String email;

    private String publicKeyPath;
    private String certificatePath;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean active;

    private String algorithm; // e.g. SHA256withRSA
}
