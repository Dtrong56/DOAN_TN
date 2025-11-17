package com.example.contract_service.dto;

import lombok.Data;


@Data
public class RegisterAndSignAppendixRequest {

    // thông tin đăng ký dịch vụ
    private String serviceId;
    private String packageId;

    // thông tin ký số
    private String signedHash;     // base64
    private String signatureValue; // base64
}



