package com.example.agreement.controller;

import com.example.agreement.service.ClaimService;
import com.example.agreement.service.dto.paymentDto.ClaimCreateRequest;
import com.example.agreement.service.dto.paymentDto.ClaimResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping("/contracts/{contractId}/claims")
    public ResponseEntity<ClaimResponseDto> createClaim(@PathVariable Long contractId,
                                                        @RequestBody ClaimCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.createClaim(contractId, request));
    }

    @GetMapping("/contracts/{contractId}/claims")
    public ResponseEntity<List<ClaimResponseDto>> getClaims(@PathVariable Long contractId) {
        return ResponseEntity.ok(claimService.getClaimsByContract(contractId));
    }

    @GetMapping("/claims/{claimId}")
    public ResponseEntity<ClaimResponseDto> getClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.getClaimById(claimId));
    }
}
