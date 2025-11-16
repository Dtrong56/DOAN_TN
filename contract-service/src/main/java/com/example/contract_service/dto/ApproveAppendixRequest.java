package com.example.contract_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class ApproveAppendixRequest {
    private String signatureValue; // Base64
    private String signedHash;     // Base64
    private List<String> certChain;
    private String signAlgorithm;  // SHA256withRSA
}

