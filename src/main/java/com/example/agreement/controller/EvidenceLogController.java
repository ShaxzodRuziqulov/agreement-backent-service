package com.example.agreement.controller;

import com.example.agreement.entity.EvidenceLog;
import com.example.agreement.service.EvidenceLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contracts/{id}/evidence")
@RequiredArgsConstructor
public class EvidenceLogController {

    private final EvidenceLogService service;

    @GetMapping
    public ResponseEntity<List<EvidenceLog>> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findByContract(id));
    }
}
