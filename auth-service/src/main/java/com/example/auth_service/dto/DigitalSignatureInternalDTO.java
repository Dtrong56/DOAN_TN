package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DigitalSignatureInternalDTO {
    private String publicKeyContent;
    private String algorithm;
    private boolean active;
}

