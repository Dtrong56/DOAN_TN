package com.example.auth_service.dto;

import lombok.Data;

@Data
public class AuthTransferUserRequest {
    private String oldUserId;       // userId của cư dân cũ cần vô hiệu hóa

    private String fullName;        // thông tin cư dân mới
    private String cccd;
    private String email;
    private String phone;
    private String tenantId;
}