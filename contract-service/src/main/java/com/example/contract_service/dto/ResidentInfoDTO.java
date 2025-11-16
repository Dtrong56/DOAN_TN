package com.example.contract_service.dto;

import lombok.Data;


@Data
public class ResidentInfoDTO {
    private String id;          // residentId
    private String apartmentId; // căn hộ cư dân đang ở
    private String buildingId;  // optional
}
