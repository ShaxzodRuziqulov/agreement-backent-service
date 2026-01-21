package com.example.agreement.entity;

import com.example.agreement.entity.enumerated.EvidenceAction;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "evidence_logs")
@Getter
@Setter
public class EvidenceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvidenceAction action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_snapshot", columnDefinition = "jsonb")
    private Map<String,Object> payloadSnapshot;

    private String ipAddress;
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
