package com.example.contract_service.dto;

import lombok.Data;

@Data
public class DigitalSignatureInternalDTO {
    private String publicKeyContent; // nội dung PEM
    private String algorithm;        // SHA256withRSA
    private boolean active;
}

