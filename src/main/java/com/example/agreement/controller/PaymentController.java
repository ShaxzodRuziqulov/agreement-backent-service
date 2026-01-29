package com.example.agreement.controller;

import com.example.agreement.service.PaymentService;
import com.example.agreement.service.dto.paymentDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/claims/{claimId}/cash")
    public ResponseEntity<PaymentResponseDto> cash(@PathVariable Long claimId,
                                                   @RequestBody CashPaymentRequest req) {
        return ResponseEntity.ok(paymentService.addCashPayment(claimId, req));
    }

    @PostMapping("/claims/{claimId}/online/init")
    public ResponseEntity<PaymentResponseDto> initOnline(@PathVariable Long claimId,
                                                         @RequestBody CardInitRequest req) {
        return ResponseEntity.ok(paymentService.initOnlinePayment(claimId, req));
    }

    // provider callback -> PERMIT ALL kerak bo'ladi
    @PostMapping("/callback")
    public ResponseEntity<PaymentResponseDto> callback(@RequestBody PaymentCallbackRequest req,
                                                       @RequestHeader(value = "X-PAYMENT-SECRET", required = false) String secret) {
        return ResponseEntity.ok(paymentService.handleCallback(req, secret));
    }

    @PostMapping("/claims/{claimId}/manual")
    public ResponseEntity<PaymentResponseDto> manual(@PathVariable Long claimId,
                                                     @RequestBody ManualTransferCreateRequest req) {
        return ResponseEntity.ok(paymentService.createManualTransfer(claimId, req));
    }

    @PostMapping("/{paymentId}/manual/review")
    public ResponseEntity<PaymentResponseDto> manualReview(@PathVariable Long paymentId,
                                                           @RequestBody ManualTransferReviewRequest req) {
        return ResponseEntity.ok(paymentService.reviewManualTransfer(paymentId, req));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> get(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
