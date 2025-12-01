package com.example.contract_service.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveExtensionResponse {
private String appendixId;
private String status;
private LocalDate approvedDate;
private String approverUserId;
private LocalDate newExpirationDate;
}
