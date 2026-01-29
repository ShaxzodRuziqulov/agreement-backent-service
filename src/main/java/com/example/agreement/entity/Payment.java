package com.example.agreement.entity;

import com.example.agreement.entity.enumerated.PaymentMethod;
import com.example.agreement.entity.enumerated.PaymentProvider;
import com.example.agreement.entity.enumerated.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "claim_id")
    private PaymentClaim claim;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Column(name = "provider_txn_id", unique = true)
    private String providerTxnId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(length = 500)
    private String note;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payer_user_id", nullable = false)
    private Long payerUserId;

    @Column(name = "payee_user_id", nullable = false)
    private Long payeeUserId;

}
