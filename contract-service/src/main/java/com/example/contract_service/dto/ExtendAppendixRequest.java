package com.example.contract_service.dto;


import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtendAppendixRequest {
private String appendixId;
private Integer extendMonths;
private String reason;
private String signatureValue;
}