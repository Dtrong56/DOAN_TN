package com.example.resident_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OwnershipTransferRequest {
    private String apartmentId;

    private String fullName;
    private String cccd;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;

    private LocalDate transferDate;
    private String note;
}
