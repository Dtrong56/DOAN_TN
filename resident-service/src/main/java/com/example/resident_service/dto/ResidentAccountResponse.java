package com.example.resident_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentAccountResponse {
    private String tenantId;
    private String residentProfileId;
}
