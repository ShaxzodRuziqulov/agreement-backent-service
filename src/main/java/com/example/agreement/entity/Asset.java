package com.example.agreement.entity;

import com.example.agreement.entity.enumerated.AssetStatus;
import com.example.agreement.entity.enumerated.AssetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "assets")
@Getter
@Setter
public class Asset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus assetStatus = AssetStatus.AVAILABLE;

    @Column(length = 2000)
    private String usageRules;

    @Column(length = 2000)
    private String damageLiability;
}