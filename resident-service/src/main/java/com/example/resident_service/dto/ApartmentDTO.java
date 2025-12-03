package com.example.resident_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ApartmentDTO {
    private String apartmentId;
    private String apartmentCode;
    private BigDecimal area;
}
