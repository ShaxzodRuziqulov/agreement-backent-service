package com.example.agreement.entity;

import com.example.agreement.entity.enumerated.PaymentClaimStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(
        name = "payment_claims",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contract_id", "period"})
)
@Getter
@Setter
public class PaymentClaim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private YearMonth period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentClaimStatus status = PaymentClaimStatus.CLAIMED;

    private LocalDateTime confirmedAt;
}
