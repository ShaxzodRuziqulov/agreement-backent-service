package com.example.agreement.service;

import com.example.agreement.entity.Contract;
import com.example.agreement.entity.Payment;
import com.example.agreement.entity.PaymentClaim;
import com.example.agreement.entity.enumerated.EvidenceAction;
import com.example.agreement.entity.enumerated.PaymentMethod;
import com.example.agreement.entity.enumerated.PaymentStatus;
import com.example.agreement.exeption.ConflictException;
import com.example.agreement.exeption.NotFoundException;
import com.example.agreement.exeption.ValidationException;
import com.example.agreement.repository.PaymentClaimRepository;
import com.example.agreement.repository.PaymentRepository;
import com.example.agreement.service.dto.paymentDto.*;
import com.example.agreement.service.paymentProvider.PaymentProviderRegistry;
import com.example.agreement.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentClaimRepository claimRepository;
    private final ClaimService claimService;
    private final EvidenceLogService evidenceLogService;
    private final PaymentProviderRegistry providerRegistry;

    @Transactional
    public PaymentResponseDto addCashPayment(Long claimId, CashPaymentRequest request) {
        if (request == null || request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Payment amount must be positive");
        }

        PaymentClaim claim = getClaim(claimId);
        assertClaimAccess(claim);

        BigDecimal unpaid = claim.getAmount().subtract(claim.getPaidAmount());
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Claim is already fully paid");
        if (request.getAmount().compareTo(unpaid) > 0) throw new ValidationException("Payment amount exceeds unpaid amount");

        Contract c = claim.getContract();
        Long payer = SecurityUtils.currentUserId();

        Payment p = new Payment();
        p.setClaim(claim);
        p.setContract(c);
        p.setAmount(request.getAmount());
        p.setMethod(PaymentMethod.CASH);
        p.setStatus(PaymentStatus.SUCCESS);
        p.setCreatedBy(payer);
        p.setPayerUserId(payer);
        p.setPayeeUserId(c.getOwner().getId());
        p.setNote(request.getNote());
        p.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(p);

        claimService.recalculateClaimStatus(claim);
        evidenceLogService.log(c, EvidenceAction.PAYMENT_SUCCESS);

        return toDto(saved);
    }

    @Transactional
    public PaymentResponseDto initOnlinePayment(Long claimId, CardInitRequest request) {
        if (request == null || request.getProvider() == null) {
            throw new ValidationException("Payment provider is required");
        }

        PaymentClaim claim = getClaim(claimId);
        assertClaimAccess(claim);

        BigDecimal unpaid = claim.getAmount().subtract(claim.getPaidAmount());
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Claim is already fully paid");

        BigDecimal amount = request.getAmount() == null ? unpaid : request.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Payment amount must be positive");
        if (amount.compareTo(unpaid) > 0) throw new ValidationException("Payment amount exceeds unpaid amount");

        Contract c = claim.getContract();
        Long payer = SecurityUtils.currentUserId();

        Payment p = new Payment();
        p.setClaim(claim);
        p.setContract(c);
        p.setAmount(amount);
        p.setMethod(PaymentMethod.CARD_ONLINE);
        p.setStatus(PaymentStatus.PENDING);
        p.setProvider(request.getProvider());
        p.setCreatedBy(payer);
        p.setPayerUserId(payer);
        p.setPayeeUserId(c.getOwner().getId());

        Payment saved = paymentRepository.save(p);

        var adapter = providerRegistry.get(saved.getProvider());

        PaymentResponseDto dto = toDto(saved);
        dto.setRedirectUrl(adapter.buildRedirectUrl(saved));
        return dto;
    }

    @Transactional
    public PaymentResponseDto handleCallback(PaymentCallbackRequest request, String secret) {
        if (request == null || request.getPaymentId() == null) throw new ValidationException("Payment id is required");
        if (request.getProvider() == null) throw new ValidationException("Provider is required");
        if (request.getProviderTxnId() == null || request.getProviderTxnId().isBlank())
            throw new ValidationException("Provider transaction id is required");

        // idempotency: providerTxnId unique
        Payment existing = paymentRepository.findByProviderTxnId(request.getProviderTxnId()).orElse(null);
        if (existing != null) return toDto(existing);

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new NotFoundException("Payment not found: " + request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return toDto(payment);
        }

        // xavfsizlik: init qilingan provider bilan callback provider mos bo‘lsin
        if (payment.getProvider() != request.getProvider()) {
            throw new ConflictException("Provider mismatch");
        }

        var adapter = providerRegistry.get(request.getProvider());
        var verified = adapter.verifyCallback(request, secret);

        if (verified.amount != null && payment.getAmount().compareTo(verified.amount) != 0) {
            throw new ValidationException("Payment amount mismatch");
        }

        payment.setProviderTxnId(verified.providerTxnId);
        payment.setStatus(verified.status == null ? PaymentStatus.FAILED : verified.status);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            payment.setPaidAt(LocalDateTime.now());
        }

        Payment saved = paymentRepository.save(payment);

        if (saved.getClaim() != null && saved.getStatus() == PaymentStatus.SUCCESS) {
            claimService.recalculateClaimStatus(saved.getClaim());
            evidenceLogService.log(saved.getContract(), EvidenceAction.PAYMENT_SUCCESS);
        } else if (saved.getClaim() != null && saved.getStatus() == PaymentStatus.FAILED) {
            evidenceLogService.log(saved.getContract(), EvidenceAction.PAYMENT_FAILED);
        }

        return toDto(saved);
    }

    // Manual transfer (screenshot/proof)
    @Transactional
    public PaymentResponseDto createManualTransfer(Long claimId, ManualTransferCreateRequest req) {
        if (req == null || req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        PaymentClaim claim = getClaim(claimId);
        assertClaimAccess(claim);

        BigDecimal unpaid = claim.getAmount().subtract(claim.getPaidAmount());
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Claim is already fully paid");
        if (req.getAmount().compareTo(unpaid) > 0) throw new ValidationException("Amount exceeds unpaid amount");

        Contract c = claim.getContract();
        Long payer = SecurityUtils.currentUserId();

        Payment p = new Payment();
        p.setClaim(claim);
        p.setContract(c);
        p.setAmount(req.getAmount());
        p.setMethod(PaymentMethod.MANUAL_TRANSFER);
        p.setStatus(PaymentStatus.PENDING_REVIEW);
        p.setCreatedBy(payer);
        p.setPayerUserId(payer);
        p.setPayeeUserId(c.getOwner().getId());
        p.setNote(req.getNote());
        // Payment entityda proofPath bo‘lmasa, note ichiga qo‘yib tur
        // lekin professional uchun proofPath column qo‘shish tavsiya
        // p.setProofPath(req.getProofPath());

        Payment saved = paymentRepository.save(p);
        return toDto(saved);
    }

    // owner/manual review
    @Transactional
    public PaymentResponseDto reviewManualTransfer(Long paymentId, ManualTransferReviewRequest req) {
        if (req == null) throw new ValidationException("Request required");

        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        if (p.getMethod() != PaymentMethod.MANUAL_TRANSFER) {
            throw new ValidationException("Not a manual transfer payment");
        }
        if (p.getStatus() != PaymentStatus.PENDING_REVIEW) {
            return toDto(p);
        }

        Long userId = SecurityUtils.currentUserId();
        boolean isOwner = p.getContract().getOwner().getId().equals(userId);
        if (!isOwner) throw new ConflictException("Only owner can review manual transfer");

        if (req.getReviewNote() != null && !req.getReviewNote().isBlank()) {
            String old = p.getNote() == null ? "" : p.getNote() + "\n";
            p.setNote(old + "REVIEW: " + req.getReviewNote());
        }

        if (req.isApprove()) {
            p.setStatus(PaymentStatus.SUCCESS);
            p.setPaidAt(LocalDateTime.now());
        } else {
            p.setStatus(PaymentStatus.REJECTED);
        }

        Payment saved = paymentRepository.save(p);

        if (saved.getClaim() != null && saved.getStatus() == PaymentStatus.SUCCESS) {
            claimService.recalculateClaimStatus(saved.getClaim());
            evidenceLogService.log(saved.getContract(), EvidenceAction.PAYMENT_SUCCESS);
        } else if (saved.getClaim() != null && saved.getStatus() == PaymentStatus.REJECTED) {
            evidenceLogService.log(saved.getContract(), EvidenceAction.PAYMENT_FAILED);
        }

        return toDto(saved);
    }

    public PaymentResponseDto getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
        return toDto(payment);
    }

    private PaymentClaim getClaim(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found: " + claimId));
    }

    private void assertClaimAccess(PaymentClaim claim) {
        Long userId = SecurityUtils.currentUserId();
        Contract c = claim.getContract();
        boolean isOwner = c.getOwner().getId().equals(userId);
        boolean isRenter = c.getRenter().getId().equals(userId);
        if (!isOwner && !isRenter) throw new ConflictException("Access denied");
    }

    private PaymentResponseDto toDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setContractId(payment.getContract().getId());
        dto.setClaimId(payment.getClaim() == null ? null : payment.getClaim().getId());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setProvider(payment.getProvider());
        dto.setProviderTxnId(payment.getProviderTxnId());
        dto.setCreatedBy(payment.getCreatedBy());
        dto.setNote(payment.getNote());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        dto.setPaidAt(payment.getPaidAt());
        return dto;
    }
}
