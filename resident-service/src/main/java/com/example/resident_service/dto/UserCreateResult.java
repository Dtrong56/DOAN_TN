package com.example.resident_service.dto;

import lombok.Data;


@Data
public class UserCreateResult {
    private String cccd;
    private String userId;
    private boolean created; // true = created, false = existed
    private String error; // null if ok
}
