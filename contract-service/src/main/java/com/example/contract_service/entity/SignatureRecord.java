package com.example.contract_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "signature_record",
       indexes = {
           @Index(name = "idx_sig_appendix", columnList = "service_appendix_id"),
           @Index(name = "idx_sig_signer", columnList = "signer_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignatureRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_appendix_id", nullable = false)
    private ServiceAppendix serviceAppendix;

    @Column(nullable = false, length = 36)
    private String signerUserId;

    @Column(nullable = false, length = 50)
    private String signerRole;

    @Column(nullable = false, length = 30)
    private String objectType;    // e.g., "APPENDIX"

    @Column(nullable = false, length = 36)
    private String objectId;

    @Column(nullable = false)
    private LocalDateTime signedAt;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String signatureFilePath;
}

