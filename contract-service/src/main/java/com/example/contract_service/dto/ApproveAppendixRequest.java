package com.example.contract_service.dto;

import lombok.Data;


@Data
public class ApproveAppendixRequest {

    private String appendixId;

    // Action: APPROVE hoặc REJECT
    private String action;

    // Chỉ required khi APPROVE
    private String signatureValue;

    // Chỉ required khi REJECT
    private String rejectReason;
}




