package com.example.auth_service.dto;

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
    private String role; // ví dụ "BQL", "ADMIN", ...
}
