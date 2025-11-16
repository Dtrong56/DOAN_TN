package com.example.resident_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResidentAccountInternalResponse {
    private String residentId;
    private String apartmentId;
    private String buildingId;
}

