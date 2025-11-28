package com.example.payment_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "debt_reminder_job")
public class DebtReminderJob extends BaseEntity {

    public enum Status {
        SCHEDULED, RUNNING, DONE, FAILED
    }

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "invoice_id", length = 36)
    private String invoiceId;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SCHEDULED;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Getters & setters
}

