package com.example.payment_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ApartmentDTO {
    private String apartmentId;
    private String apartmentCode;
    private BigDecimal area;
}
