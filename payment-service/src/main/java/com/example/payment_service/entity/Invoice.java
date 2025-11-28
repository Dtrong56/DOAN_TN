package com.example.payment_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "invoice")
public class Invoice extends BaseEntity {

    public enum Status {
        UNPAID, PAID, OVERDUE
    }

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "resident_id", length = 36, nullable = false)
    private String residentId;

    @Column(name = "apartment_id", length = 36, nullable = false)
    private String apartmentId;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "total_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.UNPAID;

    // Getters & setters
}

