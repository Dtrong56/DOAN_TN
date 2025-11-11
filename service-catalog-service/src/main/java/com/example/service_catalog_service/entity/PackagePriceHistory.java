package com.example.service_catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
    name = "package_price_history",
    uniqueConstraints = @UniqueConstraint(name = "uq_pph_pkg_eff", columnNames = {"service_package_id", "effective_from"})
)
public class PackagePriceHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_package_id", nullable = false)
    private ServicePackage servicePackage;

    @Column(name = "old_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal oldPrice;

    @Column(name = "new_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal newPrice;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    @Column(name = "changed_by_user_id", length = 36, nullable = false)
    private String changedByUserId;

    // Getters & setters
}

