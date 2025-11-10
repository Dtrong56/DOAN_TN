package com.example.resident_service.dto;

import lombok.Data;

@Data
public class ApartmentImportRow {
    private String buildingName;
    private String buildingAddress;
    private String apartmentCode;
    private Integer floor;
    private Double areaM2;
    private String residentFullName;
    private String residentCccd;
    private String residentPhone;
    private String residentEmail;
    private String residentDateOfBirth; // yyyy-MM-dd optional
}
