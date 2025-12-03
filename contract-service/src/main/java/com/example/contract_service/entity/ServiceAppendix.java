package com.example.contract_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "service_appendix",
       indexes = {
           @Index(name = "idx_appendix_contract", columnList = "main_contract_id"),
           @Index(name = "idx_appendix_tenant", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAppendix extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_contract_id", nullable = false)
    private MainContract mainContract;

    @Column(nullable = false, length = 36)
    private String serviceId;

    @Column(nullable = false, length = 36)
    private String packageId;

    @Column(nullable = false, length = 36)
    private String residentId;

    @Column(nullable = false, length = 36)
    private String apartmentId;

    @Column(nullable = false, length = 50)
    private String tenantId;

    private LocalDate signedDate;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(columnDefinition = "TEXT")
    private String signedContent;

    @Column(columnDefinition = "TEXT")
    private String residentSignature;

    @Column(columnDefinition = "TEXT")
    private String adminSignature;

    @Column(length = 36)
    private String adminUserId;

    private LocalDateTime adminApprovedAt;

    @Column(columnDefinition = "TEXT")
    private String appendixPdfPath;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, length = 30)
    private String appendixStatus;   // PENDING, SIGNED_BY_RESIDENT, WAITING_ADMIN_APPROVE, APPROVED, ACTIVE, EXPIRED, REJECTED

}

