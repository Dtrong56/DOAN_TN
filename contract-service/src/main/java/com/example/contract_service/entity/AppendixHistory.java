package com.example.contract_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appendix_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"service_appendix_id", "version_no"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppendixHistory {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_appendix_id", nullable = false)
    private ServiceAppendix serviceAppendix;

    @Column(nullable = false)
    private int versionNo;

    @Column(nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    @Column(nullable = false)
    private UUID changedByUserId;

    @Column(nullable = false, length = 50)
    private String changeType;

    private LocalDate oldEffectiveDate;
    private LocalDate newEffectiveDate;
    private LocalDate oldExpirationDate;
    private LocalDate newExpirationDate;

    private UUID oldPackageId;
    private UUID newPackageId;

    @Column(columnDefinition = "TEXT")
    private String note;
}
