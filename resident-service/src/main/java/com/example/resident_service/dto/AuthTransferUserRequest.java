package com.example.resident_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthTransferUserRequest {
    private String oldUserId;       // userId của cư dân cũ cần vô hiệu hóa

    private String fullName;        // thông tin cư dân mới
    private String cccd;
    private String email;
    private String phone;
    private String tenantId;
}