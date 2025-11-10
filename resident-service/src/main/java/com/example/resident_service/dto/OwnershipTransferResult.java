package com.example.resident_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OwnershipTransferResult {

    // trạng thái kết quả
    private boolean success;
    private String message;

    // thông tin căn hộ
    private String apartmentId;
    private String apartmentCode;

    // chủ cũ
    private String oldOwnerFullName;
    private String oldOwnerCccd;

    // chủ mới
    private String newOwnerFullName;
    private String newOwnerCccd;

    // userId mới trả về từ auth-service nếu có tạo mới
    private String newOwnerUserId; 
}
