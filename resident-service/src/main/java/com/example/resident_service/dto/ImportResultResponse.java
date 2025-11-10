package com.example.resident_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportResultResponse {
    private boolean success;
    private String message;
    private int createdCount;
}
