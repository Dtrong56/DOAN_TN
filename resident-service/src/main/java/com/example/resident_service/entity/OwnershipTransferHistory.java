package com.example.resident_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;

@Entity
@Table(name = "ownership_transfer_history",
       indexes = @Index(name = "idx_oth_apartment", columnList = "apartment_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnershipTransferHistory extends BaseEntity {

    @Column(name = "apartment_id", columnDefinition = "CHAR(36)", nullable = false)
    private String apartmentId;

    @Column(name = "from_resident_id", columnDefinition = "CHAR(36)", nullable = false)
    private String fromResidentId;

    @Column(name = "to_resident_id", columnDefinition = "CHAR(36)", nullable = false)
    private String toResidentId;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(columnDefinition = "TEXT")
    private String note;
}
