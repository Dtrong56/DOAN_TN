package com.example.contract_service.dto;

import lombok.Data;


@Data
public class RegisterAndSignAppendixRequest {
    private String serviceId;
    private String packageId;

    private String signatureValue; // chỉ signatureValue base64
}




