package com.example.agreement.controller;

import com.example.agreement.service.ContractDocumentService;
import com.example.agreement.service.dto.contractDocumentDto.ContractDocumentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts/{contractId}/documents")
@RequiredArgsConstructor
public class ContractDocumentController {

    private final ContractDocumentService service;

    @PostMapping
    public ResponseEntity<ContractDocumentDto> create(@PathVariable Long contractId, @RequestParam String content, @RequestParam String pdfPath) {
        return ResponseEntity.ok(service.create(contractId, content, pdfPath));
    }

    @GetMapping
    public ResponseEntity<List<ContractDocumentDto>> getAll(@PathVariable Long contractId) {
        return ResponseEntity.ok(service.findByContract(contractId));
    }
}