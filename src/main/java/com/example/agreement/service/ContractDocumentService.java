package com.example.agreement.service;

import com.example.agreement.entity.Contract;
import com.example.agreement.entity.ContractDocument;
import com.example.agreement.repository.ContractDocumentRepository;
import com.example.agreement.repository.ContractRepository;
import com.example.agreement.service.dto.contractDocumentDto.ContractDocumentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractDocumentService {

    private final ContractRepository contractRepository;
    private final ContractDocumentRepository repository;

    @Transactional
    public ContractDocumentDto create(Long contractId, String content, String pdfPath) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        Integer nextVersion = repository.findMaxVersionByContractId(contractId)
                .map(v -> v + 1)
                .orElse(1);

        ContractDocument doc = new ContractDocument();
        doc.setContract(contract);
        doc.setContent(content);
        doc.setPdfPath(pdfPath);
        doc.setVersion(nextVersion);

        ContractDocument saved = repository.save(doc);
        return toDto(saved);
    }

    public List<ContractDocumentDto> findByContract(Long contractId) {
        return repository.findByContractIdOrderByVersionDesc(contractId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ContractDocumentDto toDto(ContractDocument doc) {
        ContractDocumentDto dto = new ContractDocumentDto();
        dto.setId(doc.getId());
        dto.setContractId(doc.getContract().getId());
        dto.setContent(doc.getContent());
        dto.setPdfPath(doc.getPdfPath());
        dto.setVersion(doc.getVersion());
        dto.setCreatedAt(doc.getCreatedAt());
        return dto;
    }
}