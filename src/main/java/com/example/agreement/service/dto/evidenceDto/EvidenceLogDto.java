package com.example.agreement.service.dto.evidenceDto;

import com.example.agreement.entity.Contract;
import com.example.agreement.entity.enumerated.EvidenceAction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvidenceLogDto {
    private Long id;
    private Contract contract;
    private EvidenceAction action;
    private String description;
}
