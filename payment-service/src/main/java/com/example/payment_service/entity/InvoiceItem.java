package com.example.payment_service.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_item")
public class InvoiceItem {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "service_appendix_id", length = 36)
    private String serviceAppendixId;

    // Getters & setters
}

