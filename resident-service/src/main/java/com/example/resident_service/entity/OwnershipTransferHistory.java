package com.example.resident_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ownership_transfer_history",
       indexes = @Index(name = "idx_oth_apartment", columnList = "apartment_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnershipTransferHistory {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "apartment_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID apartmentId;

    @Column(name = "from_resident_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID fromResidentId;

    @Column(name = "to_resident_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID toResidentId;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(columnDefinition = "TEXT")
    private String note;
}
