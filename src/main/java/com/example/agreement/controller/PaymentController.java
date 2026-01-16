package com.example.agreement.controller;

import com.example.agreement.service.PaymentClaimService;
import com.example.agreement.service.dto.paymentDto.PaymentClaimCreateDto;
import com.example.agreement.service.dto.paymentDto.PaymentClaimResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentClaimService service;

    @PostMapping("/api/contracts/{id}/payments")
    public ResponseEntity<PaymentClaimResponseDto> claim(
            @PathVariable Long id,
            @RequestBody PaymentClaimCreateDto dto
    ) {
        return ResponseEntity.ok(service.claim(id, dto));
    }

    @PostMapping("/api/payments/{id}/confirm")
    public ResponseEntity<PaymentClaimResponseDto> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirm(id));
    }

    @GetMapping("/api/contracts/{id}/payments")
    public ResponseEntity<List<PaymentClaimResponseDto>> list(@PathVariable Long id) {
        return ResponseEntity.ok(service.findByContract(id));
    }
}
