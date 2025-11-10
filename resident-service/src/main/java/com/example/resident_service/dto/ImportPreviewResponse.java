package com.example.resident_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ImportPreviewResponse {
    private String tenantId;
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private List<ApartmentImportRow> previewRows;
    private List<Map<String,String>> errors; // each entry: {row, message}
}
