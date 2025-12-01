package com.example.contract_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveExtensionRequest {
private String appendixId;
private String action; // APPROVE / REJECT
private String signatureValue; // required if APPROVE
private String rejectReason; // required if REJECT
}
