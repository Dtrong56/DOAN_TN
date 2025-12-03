package com.example.resident_service.dto;

import lombok.Data;

@Data
public class ResidentDTO {
    private String residentId;
    private String fullName;
    private String email;
    private String phone;
    private ApartmentDTO apartment;
}
