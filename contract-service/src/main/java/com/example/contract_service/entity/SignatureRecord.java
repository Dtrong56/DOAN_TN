package com.example.contract_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signature_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignatureRecord {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_appendix_id", nullable = false)
    private ServiceAppendix serviceAppendix;

    @Column(nullable = false)
    private UUID signerUserId;

    @Column(nullable = false, length = 50)
    private String signerRole;

    @Column(nullable = false)
    private LocalDateTime signedAt = LocalDateTime.now();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String signatureFilePath;
}
