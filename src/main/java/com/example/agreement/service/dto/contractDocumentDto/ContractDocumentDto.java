package com.example.agreement.service.dto.contractDocumentDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ContractDocumentDto {
    private Long id;
    private Long contractId;
    private String content;
    private String pdfPath;
    private Integer version;
    private LocalDateTime createdAt;
}

