package com.example.agreement.service.dto.evidenceDto;

import com.example.agreement.entity.enumerated.EvidenceAction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EvidenceResponseDto {

    private EvidenceAction action;
    private Long actorUserId;

    private String payloadSnapshot;
    private LocalDateTime createdAt;
}

