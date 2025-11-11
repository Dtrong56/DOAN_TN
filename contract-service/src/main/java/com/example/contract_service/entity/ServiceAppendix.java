package com.example.contract_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "service_appendix")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAppendix extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_contract_id", nullable = false)
    private MainContract mainContract;

    @Column(nullable = false)
    private String serviceId;

    @Column(nullable = false)
    private String packageId;

    @Column(nullable = false)
    private String residentId;

    @Column(nullable = false)
    private String apartmentId;

    private LocalDate signedDate;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDate expirationDate;
}
