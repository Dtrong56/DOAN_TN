package com.example.resident_service.dto;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentAccountResponse {
    private UUID tenantId;
    private UUID residentProfileId;
}
