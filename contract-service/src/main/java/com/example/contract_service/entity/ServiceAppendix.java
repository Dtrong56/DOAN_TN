package com.example.contract_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "service_appendix")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAppendix {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_contract_id", nullable = false)
    private MainContract mainContract;

    @Column(nullable = false)
    private UUID serviceId;

    @Column(nullable = false)
    private UUID packageId;

    @Column(nullable = false)
    private UUID residentId;

    @Column(nullable = false)
    private UUID apartmentId;

    private LocalDate signedDate;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDate expirationDate;
}
