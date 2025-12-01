package com.example.contract_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtendAppendixResponse {
private String appendixId;
private String status;
private LocalDateTime requestedAt;
}
