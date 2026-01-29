//package com.example.agreement.controller;
//
//import com.example.agreement.service.PaymentClaimService;
//import com.example.agreement.service.dto.paymentDto.CashPaymentRequest;
//import com.example.agreement.service.dto.paymentDto.ClaimResponseDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/payment-claims")
//@RequiredArgsConstructor
//public class PaymentClaimController {
//
//    private final PaymentClaimService service;
//
//    @PostMapping("/contracts/{contractId}/claim")
//    public ResponseEntity<ClaimResponseDto> createClaim(@PathVariable Long contractId,
//                                                        @RequestBody CashPaymentRequest dto) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(service.createClaim(contractId, dto));
//    }
//
//    @PostMapping("/{claimId}/paid")
//    public ResponseEntity<ClaimResponseDto> markAsPaid(@PathVariable Long claimId) {
//        return ResponseEntity.ok(service.markAsPaid(claimId));
//    }
//
//    @PostMapping("/{claimId}/confirm")
//    public ResponseEntity<ClaimResponseDto> confirm(@PathVariable Long claimId) {
//        return ResponseEntity.ok(service.confirm(claimId));
//    }
//
//    @PostMapping("/{claimId}/reject")
//    public ResponseEntity<ClaimResponseDto> reject(@PathVariable Long claimId) {
//        return ResponseEntity.ok(service.reject(claimId));
//    }
//
//    @GetMapping("/contracts/{contractId}")
//    public ResponseEntity<List<ClaimResponseDto>> findByContract(@PathVariable Long contractId) {
//        return ResponseEntity.ok(service.findByContract(contractId));
//    }
//}
