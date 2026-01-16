package com.example.agreement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Table(name = "contract_documents")
@Getter
@Setter
public class ContractDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(nullable = false, length = 12000)
    private String content;

    @Column(nullable = false)
    private String pdfPath;

    @Column(nullable = false)
    private Integer version;

    private String aiModel;
    private String promptHash;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
