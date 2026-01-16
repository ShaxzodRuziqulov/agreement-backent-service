package com.example.agreement.entity;

import com.example.agreement.entity.enumerated.ContractStatus;
import com.example.agreement.entity.enumerated.PaymentPeriod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "contracts")
@Getter
@Setter
public class Contract extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(optional = false)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentPeriod paymentPeriod;

    @Column(nullable = false)
    private Integer paymentDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.DRAFT;

    private LocalDateTime acceptedAt;

    @Column(nullable = false, length = 5)
    private String jurisdiction = "UZ";

    @Column(nullable = false, length = 5)
    private String language = "uz";
}
