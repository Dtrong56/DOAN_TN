package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "credential")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credential extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hashed_password", nullable = false, length = 512)
    private String hashedPassword;

    @Column(name = "last_password_change")
    private Instant lastPasswordChange;
}
