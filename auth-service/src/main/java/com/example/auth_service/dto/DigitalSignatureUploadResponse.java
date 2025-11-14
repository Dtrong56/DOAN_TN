package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSignatureUploadResponse {

    private String signatureId;
    private String publicKeyPath;
    private String certificatePath;
    private LocalDate validFrom;
    private LocalDate validTo;
}



