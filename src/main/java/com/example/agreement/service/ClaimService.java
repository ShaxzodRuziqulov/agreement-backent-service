package com.example.agreement.service;

import com.example.agreement.entity.Contract;
import com.example.agreement.entity.Payment;
import com.example.agreement.entity.PaymentClaim;
import com.example.agreement.entity.enumerated.BillingUnit;
import com.example.agreement.entity.enumerated.ContractStatus;
import com.example.agreement.entity.enumerated.EvidenceAction;
import com.example.agreement.entity.enumerated.PaymentClaimStatus;
import com.example.agreement.entity.enumerated.PaymentStatus;
import com.example.agreement.exeption.ConflictException;
import com.example.agreement.exeption.ContractNotFoundException;
import com.example.agreement.exeption.NotFoundException;
import com.example.agreement.exeption.ValidationException;
import com.example.agreement.repository.ContractRepository;
import com.example.agreement.repository.PaymentClaimRepository;
import com.example.agreement.repository.PaymentRepository;
import com.example.agreement.service.dto.paymentDto.ClaimCreateRequest;
import com.example.agreement.service.dto.paymentDto.ClaimResponseDto;
import com.example.agreement.service.dto.paymentDto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final PaymentClaimRepository repository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final EvidenceLogService evidenceLogService;

    @Transactional
    public PaymentClaim createInitialClaim(Contract contract, LocalDate periodStart) {
        LocalDate alignedStart = alignPeriodStart(contract.getBillingUnit(), periodStart);

        if (repository.existsByContractIdAndPeriodStart(contract.getId(), alignedStart)) {
            throw new ConflictException("Claim already exists for this period");
        }

        PaymentClaim claim = new PaymentClaim();
        claim.setContract(contract);
        claim.setPeriodStart(alignedStart);
        claim.setPeriodEnd(resolvePeriodEnd(contract.getBillingUnit(), alignedStart));
        claim.setAmount(contract.getBillingAmount());
        claim.setDueAt(claim.getPeriodEnd().atStartOfDay());
        claim.setStatus(PaymentClaimStatus.OPEN);
        claim.setPaidAmount(BigDecimal.ZERO);

        PaymentClaim saved = repository.save(claim);
        evidenceLogService.log(contract, EvidenceAction.PAYMENT_CLAIM_CREATED);
        return saved;
    }

    @Transactional
    public ClaimResponseDto createClaim(Long contractId, ClaimCreateRequest request) {
        Contract contract = getContract(contractId);
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new ConflictException("Claims can only be created for ACTIVE contracts");
        }

        if (request == null || request.getPeriodStart() == null) {
            throw new ValidationException("Period start is required");
        }

        LocalDate periodStart = alignPeriodStart(contract.getBillingUnit(), request.getPeriodStart());
        if (repository.existsByContractIdAndPeriodStart(contractId, periodStart)) {
            throw new ConflictException("Claim already exists for this period");
        }

        PaymentClaim claim = new PaymentClaim();
        claim.setContract(contract);
        claim.setPeriodStart(periodStart);
        claim.setPeriodEnd(resolvePeriodEnd(contract.getBillingUnit(), periodStart));
        claim.setAmount(contract.getBillingAmount());
        claim.setDueAt(claim.getPeriodEnd().atStartOfDay());
        claim.setStatus(PaymentClaimStatus.OPEN);
        claim.setPaidAmount(BigDecimal.ZERO);

        PaymentClaim saved = repository.save(claim);
        evidenceLogService.log(contract, EvidenceAction.PAYMENT_CLAIM_CREATED);
        return toDto(saved, false);
    }

    public ClaimResponseDto getClaimById(Long claimId) {
        PaymentClaim claim = repository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found: " + claimId));
        return toDto(claim, true);
    }

    public List<ClaimResponseDto> getClaimsByContract(Long contractId) {
        return repository.findByContractIdOrderByPeriodStartDesc(contractId)
                .stream()
                .map(claim -> toDto(claim, false))
                .toList();
    }

    @Transactional
    public void recalculateClaimStatus(PaymentClaim claim) {
        BigDecimal totalPaid = paymentRepository.sumAmountByClaimAndStatus(
                claim.getId(), PaymentStatus.SUCCESS
        );
        claim.setPaidAmount(totalPaid);

        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            claim.setStatus(PaymentClaimStatus.OPEN);
        } else if (totalPaid.compareTo(claim.getAmount()) < 0) {
            claim.setStatus(PaymentClaimStatus.PARTIALLY_PAID);
        } else {
            claim.setStatus(PaymentClaimStatus.PAID);
        }

        repository.save(claim);
    }

    private Contract getContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException("Contract not found: " + id));
    }

    private LocalDate alignPeriodStart(BillingUnit unit, LocalDate periodStart) {
        LocalDate base = periodStart == null ? LocalDate.now() : periodStart;
        if (unit == BillingUnit.MONTH) {
            return base.withDayOfMonth(1);
        }
        if (unit == BillingUnit.WEEK) {
            return base.with(DayOfWeek.MONDAY);
        }
        return base;
    }

    private LocalDate resolvePeriodEnd(BillingUnit unit, LocalDate periodStart) {
        if (unit == BillingUnit.MONTH) {
            return periodStart.withDayOfMonth(periodStart.lengthOfMonth());
        }
        if (unit == BillingUnit.WEEK) {
            return periodStart.plusDays(6);
        }
        return periodStart;
    }

    private ClaimResponseDto toDto(PaymentClaim claim, boolean includePayments) {
        ClaimResponseDto dto = new ClaimResponseDto();
        dto.setId(claim.getId());
        dto.setContractId(claim.getContract().getId());
        dto.setPeriodStart(claim.getPeriodStart());
        dto.setPeriodEnd(claim.getPeriodEnd());
        dto.setAmount(claim.getAmount());
        dto.setPaidAmount(claim.getPaidAmount());
        dto.setStatus(claim.getStatus());
        dto.setDueAt(claim.getDueAt());

        if (includePayments) {
            List<PaymentResponseDto> payments = claim.getPayments().stream()
                    .map(this::toPaymentDto)
                    .toList();
            dto.setPayments(payments);
        }

        return dto;
    }

    private PaymentResponseDto toPaymentDto(Payment payment) {
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
