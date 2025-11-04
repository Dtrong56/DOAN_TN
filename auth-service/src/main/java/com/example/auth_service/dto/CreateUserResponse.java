package com.example.auth_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserResponse {
    private String userId;
    private String username;
    private String email;
    private String role;
}
