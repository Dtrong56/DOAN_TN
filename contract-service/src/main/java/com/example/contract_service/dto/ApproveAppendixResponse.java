package com.example.contract_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ApproveAppendixResponse {
    private String appendixId;
    private String status; // ACTIVE or REJECTED
    private LocalDate approvedDate;
    private String approverUserId;
}

