package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id = UUID.randomUUID().toString();
}
