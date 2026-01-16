package com.example.agreement.service;

import com.example.agreement.entity.Contract;
import com.example.agreement.entity.PaymentClaim;
import com.example.agreement.entity.User;
import com.example.agreement.repository.ContractRepository;
import com.example.agreement.repository.PaymentClaimRepository;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.dto.paymentDto.PaymentClaimCreateDto;
import com.example.agreement.service.dto.paymentDto.PaymentClaimResponseDto;
import com.example.agreement.entity.enumerated.EvidenceAction;
import com.example.agreement.entity.enumerated.PaymentClaimStatus;
import com.example.agreement.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentClaimService {

    private final PaymentClaimRepository repository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final EvidenceLogService evidenceLogService;

    @Transactional
    public PaymentClaimResponseDto claim(Long contractId, PaymentClaimCreateDto dto) {
        User renter = getCurrentUser();
        Contract contract = getContract(contractId);

        if (!contract.getRenter().getId().equals(renter.getId())) {
            throw new IllegalStateException("Only renter can claim payment");
        }

        PaymentClaim claim = new PaymentClaim();
        claim.setContract(contract);
        claim.setPeriod(dto.getPeriod());
        claim.setAmount(dto.getAmount());
        claim.setStatus(PaymentClaimStatus.CLAIMED);

        PaymentClaim saved = repository.save(claim);
        evidenceLogService.log(contract, EvidenceAction.PAYMENT_CLAIMED);

        return toDto(saved);
    }

    @Transactional
    public PaymentClaimResponseDto confirm(Long claimId) {
        User owner = getCurrentUser();
        PaymentClaim claim = repository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        if (!claim.getContract().getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("Only owner can confirm payment");
        }

        claim.setStatus(PaymentClaimStatus.CONFIRMED);
        claim.setConfirmedAt(LocalDateTime.now());

        PaymentClaim saved = repository.save(claim);
        evidenceLogService.log(saved.getContract(), EvidenceAction.PAYMENT_CONFIRMED);

        return toDto(saved);
    }

    public List<PaymentClaimResponseDto> findByContract(Long contractId) {
        return repository.findByContractIdOrderByPeriodDesc(contractId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /* ===== helpers ===== */

    private Contract getContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private PaymentClaimResponseDto toDto(PaymentClaim p) {
        PaymentClaimResponseDto dto = new PaymentClaimResponseDto();
        dto.setId(p.getId());
        dto.setPeriod(p.getPeriod());
        dto.setAmount(p.getAmount());
        dto.setStatus(p.getStatus());
        dto.setConfirmedAt(p.getConfirmedAt());
        return dto;
    }
}
