package com.example.multi_tenant_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;
}
