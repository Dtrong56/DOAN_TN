package com.example.monitoring_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLogResponseDTO {

    private String id;
    private LocalDateTime timestamp;

    private String userId;
    private String tenantId;
    private String action;
    private String objectType;
    private String objectId;

    private String description;
}
