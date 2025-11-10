package com.example.resident_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImportConfirmRequest {
    private List<ApartmentImportRow> apartments;
}
