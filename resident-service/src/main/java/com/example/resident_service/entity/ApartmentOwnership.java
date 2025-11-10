package com.example.resident_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;

@Entity
@Table(name = "apartment_ownership",
       indexes = {
           @Index(name = "idx_own_apartment", columnList = "apartment_id"),
           @Index(name = "idx_own_resident", columnList = "resident_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentOwnership extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @Column(name = "resident_id", columnDefinition = "CHAR(36)", nullable = false)
    private String residentId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_representative", nullable = false)
    private Boolean isRepresentative = false;
}
