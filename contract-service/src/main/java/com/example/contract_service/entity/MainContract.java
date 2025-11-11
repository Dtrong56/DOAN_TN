package com.example.contract_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "main_contract")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainContract extends BaseEntity {

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true, length = 100)
    private String contractCode;

    @Column(nullable = false)
    private LocalDate signedDate;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(columnDefinition = "TEXT")
    private String pdfFilePath;
}
