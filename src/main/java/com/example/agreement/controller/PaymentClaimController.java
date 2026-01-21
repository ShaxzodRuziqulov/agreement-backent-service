package com.example.agreement.controller;

import com.example.agreement.service.PaymentClaimService;
import com.example.agreement.service.dto.paymentDto.PaymentClaimCreateDto;
import com.example.agreement.service.dto.paymentDto.PaymentClaimResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-claims")
@RequiredArgsConstructor
public class PaymentClaimController {

    private final PaymentClaimService service;

    /**
     * ✅ OWNER creates claim for contract period
     * POST /api/payment-claims/contracts/{contractId}/claim
     */
    @PostMapping("/contracts/{contractId}/claim")
    public ResponseEntity<PaymentClaimResponseDto> createClaim(@PathVariable Long contractId,
                                                               @RequestBody PaymentClaimCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createClaim(contractId, dto));
    }

    /**
     * ✅ RENTER marks claim as PAID
     * POST /api/payment-claims/{claimId}/paid
     */
    @PostMapping("/{claimId}/paid")
    public ResponseEntity<PaymentClaimResponseDto> markAsPaid(@PathVariable Long claimId) {
        return ResponseEntity.ok(service.markAsPaid(claimId));
    }

    /**
     * ✅ OWNER confirms payment (CONFIRMED)
     * POST /api/payment-claims/{claimId}/confirm
     */
    @PostMapping("/{claimId}/confirm")
    public ResponseEntity<PaymentClaimResponseDto> confirm(@PathVariable Long claimId) {
        return ResponseEntity.ok(service.confirm(claimId));
    }

    /**
     * ✅ OWNER rejects payment (REJECTED)
     * POST /api/payment-claims/{claimId}/reject
     */
    @PostMapping("/{claimId}/reject")
    public ResponseEntity<PaymentClaimResponseDto> reject(@PathVariable Long claimId) {
        return ResponseEntity.ok(service.reject(claimId));
    }

    /**
     * ✅ Get claims by contract
     * GET /api/payment-claims/contracts/{contractId}
     */
    @GetMapping("/contracts/{contractId}")
    public ResponseEntity<List<PaymentClaimResponseDto>> findByContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(service.findByContract(contractId));
    }
}
