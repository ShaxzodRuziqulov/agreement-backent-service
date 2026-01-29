package com.example.agreement.entity;

import com.example.agreement.entity.enumerated.BillingUnit;
import com.example.agreement.entity.enumerated.ContractStatus;
import com.example.agreement.entity.enumerated.PaymentPeriod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
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

    @Column(name = "billing_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal billingAmount;

    @Column(name = "billing_unit", nullable = false)
    private BillingUnit billingUnit;

    @Column(name = "prepaid_periods", nullable = false)
    private Integer prepaidPeriods = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(nullable = false, length = 5)
    private String jurisdiction = "UZ";

    @Column(nullable = false, length = 5)
    private String language = "uz";
}
