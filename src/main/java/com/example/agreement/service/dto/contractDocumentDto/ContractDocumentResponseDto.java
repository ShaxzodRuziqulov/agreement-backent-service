package com.example.agreement.service.dto.contractDocumentDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ContractDocumentResponseDto {

    private Integer version;
    private String content;
    private String pdfPath;

    private LocalDateTime generatedAt;
}

