package com.example.agreement.service;

import com.example.agreement.entity.Asset;
import com.example.agreement.entity.Contract;
import com.example.agreement.entity.PaymentClaim;
import com.example.agreement.entity.User;
import com.example.agreement.entity.enumerated.ContractStatus;
import com.example.agreement.entity.enumerated.EvidenceAction;
import com.example.agreement.entity.enumerated.PaymentMethod;
import com.example.agreement.entity.enumerated.VerificationStatus;
import com.example.agreement.exeption.ConflictException;
import com.example.agreement.exeption.ContractNotFoundException;
import com.example.agreement.exeption.NotFoundException;
import com.example.agreement.exeption.ValidationException;
import com.example.agreement.repository.ContractRepository;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.dto.contractDto.ContractActivateRequest;
import com.example.agreement.service.dto.contractDto.ContractCreateDto;
import com.example.agreement.service.dto.contractDto.ContractResponseDto;
import com.example.agreement.service.dto.paymentDto.CashPaymentRequest;
import com.example.agreement.service.dto.paymentDto.ClaimResponseDto;
import com.example.agreement.service.dto.userDto.PinflSubmitDto;
import com.example.agreement.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository repository;
    private final UserRepository userRepository;
    private final AssetService assetService;
    private final EvidenceLogService evidenceLogService;
    private final ClaimService claimService;
    private final PaymentService paymentService;

    @Transactional
    public ContractResponseDto create(ContractCreateDto dto) {
        if (dto == null) throw new ValidationException("Request is required");

        User owner = getCurrentUser();

        if (dto.getRenterId() == null) throw new ValidationException("RenterId is required");
        if (owner.getId().equals(dto.getRenterId())) {
            throw new ValidationException("Owner and renter cannot be the same user");
        }

        if (dto.getBillingAmount() == null || dto.getBillingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Billing amount must be positive");
        }
        if (dto.getBillingUnit() == null) {
            throw new ValidationException("Billing unit is required");
        }
        if (dto.getPrepaidPeriods() != null && dto.getPrepaidPeriods() <= 0) {
            throw new ValidationException("Prepaid periods must be positive");
        }

        Asset asset = assetService.getAsset(dto.getAssetId());

        User renter = userRepository.findById(dto.getRenterId())
                .orElseThrow(() -> new NotFoundException("Renter not found: " + dto.getRenterId()));

        Contract contract = new Contract();
        contract.setOwner(owner);
        contract.setRenter(renter);
        contract.setAsset(asset);
        contract.setBillingAmount(dto.getBillingAmount());
        contract.setBillingUnit(dto.getBillingUnit());
        contract.setPrepaidPeriods(dto.getPrepaidPeriods() == null ? 1 : dto.getPrepaidPeriods());
        contract.setLanguage(dto.getLanguage());
        contract.setStatus(ContractStatus.DRAFT);

        Contract saved = repository.save(contract);
        evidenceLogService.log(saved, EvidenceAction.CONTRACT_CREATED);

        return toDto(saved);
    }

    // =========================
    // 2) SEND TO RENTER: DRAFT -> WAITING_RENTER_INFO
    // =========================
    @Transactional
    public void sendToRenter(Long contractId) {
        Contract contract = getContract(contractId);
        User owner = getCurrentUser();

        if (!contract.getOwner().getId().equals(owner.getId())) {
            throw new ConflictException("Only owner can send contract to renter");
        }
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new ConflictException("Contract must be DRAFT to send to renter");
        }

        contract.setStatus(ContractStatus.WAITING_RENTER_INFO);
        repository.save(contract);

        evidenceLogService.log(contract, EvidenceAction.CONTRACT_SENT_TO_RENTER);
    }

    // =========================
    // 3) RENTER SUBMIT: WAITING_RENTER_INFO -> WAITING_OWNER_APPROVAL
    // =========================
    @Transactional
    public void renterSubmit(Long contractId, PinflSubmitDto request) {
        User renter = getCurrentUser();
        Contract contract = getContract(contractId);

        if (!contract.getRenter().getId().equals(renter.getId())) {
            throw new ConflictException("Only assigned renter can submit");
        }
        if (contract.getStatus() != ContractStatus.WAITING_RENTER_INFO) {
            throw new ConflictException("Contract is not waiting for renter info");
        }

        // passport images upload bo‘lgan bo‘lishi shart (upload endpoint sening loyihangda alohida bo‘ladi)
        if (renter.getPassportFrontPath() == null || renter.getPassportBackPath() == null) {
            throw new ValidationException("Passport images are required");
        }

        if (request == null || request.getPinfl() == null || request.getPinfl().isBlank()) {
            throw new ValidationException("PINFL is required");
        }

        String pinfl = request.getPinfl().trim();
        if (!pinfl.matches("\\d{14}")) {
            throw new ValidationException("PINFL must be 14 digits");
        }

        if (renter.getPinfl() == null || renter.getPinfl().isBlank()) {
            if (userRepository.existsByPinfl(pinfl)) {
                throw new ConflictException("PINFL already used by another user");
            }
            renter.setPinfl(pinfl);
            userRepository.save(renter);
        }

        contract.setStatus(ContractStatus.WAITING_OWNER_APPROVAL);
        repository.save(contract);

        evidenceLogService.log(contract, EvidenceAction.RENTER_SUBMITTED_INFO);
    }

    // =========================
    // 4) OWNER APPROVE: WAITING_OWNER_APPROVAL -> APPROVED
    // =========================
    @Transactional
    public void ownerApprove(Long contractId) {
        User owner = getCurrentUser();
        Contract contract = getContract(contractId);

        if (!contract.getOwner().getId().equals(owner.getId())) {
            throw new ConflictException("Only owner can approve");
        }
        if (contract.getStatus() != ContractStatus.WAITING_OWNER_APPROVAL) {
            throw new ConflictException("Contract is not waiting for owner approval");
        }

        User renter = contract.getRenter();
        if (renter.getPassportFrontPath() == null || renter.getPassportBackPath() == null) {
            throw new ValidationException("Renter passport images are missing");
        }
        if (renter.getPinfl() == null || renter.getPinfl().isBlank()) {
            throw new ValidationException("Renter pinfl is missing");
        }

        // Bu yerda ACTIVE qilinmaydi!
        contract.setStatus(ContractStatus.APPROVED);
        repository.save(contract);

        evidenceLogService.log(contract, EvidenceAction.OWNER_APPROVED_CONTRACT);
    }

    // =========================
    // 5) ACTIVATE: APPROVED -> ACTIVE + initial claim + prepaid cash payment
    // =========================
    @Transactional
    public ClaimResponseDto activate(Long contractId, ContractActivateRequest request) {
        if (request == null) throw new ValidationException("Activation request is required");

        Contract contract = getContract(contractId);

        if (contract.getStatus() != ContractStatus.APPROVED) {
            throw new ConflictException("Contract must be APPROVED to activate");
        }

        User owner = getCurrentUser();
        if (!contract.getOwner().getId().equals(owner.getId())) {
            throw new ConflictException("Only owner can activate contract");
        }

        BigDecimal prepaidAmount = request.getPrepaidAmount();
        if (prepaidAmount == null || prepaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Prepaid amount must be positive");
        }

        if (prepaidAmount.compareTo(contract.getBillingAmount()) < 0) {
            throw new ValidationException("Prepaid amount must be at least billing amount");
        }

        validateRenterIdentity(contract, true);

        PaymentMethod method = request.getPrepaidMethod() == null ? PaymentMethod.CASH : request.getPrepaidMethod();
        if (method != PaymentMethod.CASH) {
            throw new ConflictException("Activation requires CASH prepaid payment");
        }

        LocalDate periodStart = request.getPeriodStart(); // null bo‘lsa ClaimService default qilsin (yoki bu yerda tekshir)
        PaymentClaim claim = claimService.createInitialClaim(contract, periodStart);
//
//        // claim yaratilganini log qilmoqchi bo‘lsang (ClaimService ichida ham bo‘lishi mumkin)
//        evidenceLogService.log(contract, EvidenceAction.PAYMENT_CLAIM_CREATED);

        CashPaymentRequest paymentRequest = new CashPaymentRequest();
        paymentRequest.setAmount(prepaidAmount);
        paymentRequest.setNote(request.getHandoverNote());

        paymentService.addCashPayment(claim.getId(), paymentRequest);

        contract.setStatus(ContractStatus.ACTIVE);
        contract.setStartAt(LocalDateTime.now());
        repository.save(contract);

        evidenceLogService.log(contract, EvidenceAction.CONTRACT_ACTIVATED);

        return claimService.getClaimById(claim.getId());
    }

    // =========================
    // Optional: OWNER verifies renter PINFL (manual)
    // =========================
    @Transactional
    public void verifyRenterIdentity(Long contractId) {
        Contract contract = getContract(contractId);
        User owner = getCurrentUser();

        if (!contract.getOwner().getId().equals(owner.getId())) {
            throw new ConflictException("Only owner can verify renter identity");
        }

//        validateRenterIdentity(contract, false);

        User renter = contract.getRenter();
        renter.setPinflStatus(VerificationStatus.VERIFIED);
        renter.setPassportStatus(VerificationStatus.VERIFIED);
        renter.setPinflVerifiedAt(LocalDateTime.now());
        userRepository.save(renter);

        evidenceLogService.log(contract, EvidenceAction.RENTER_IDENTITY_VERIFIED);
    }

    // =========================
    // 6) CLOSE: ACTIVE (yoki LATE/SUSPENDED) -> CLOSED
    // =========================
    @Transactional
    public void closeContract(Long contractId) {
        User user = getCurrentUser();
        Contract contract = getContract(contractId);

        if (contract.getStatus() == ContractStatus.CLOSED) {
            throw new ConflictException("Contract already closed");
        }

        boolean isOwner = contract.getOwner().getId().equals(user.getId());
        boolean isRenter = contract.getRenter().getId().equals(user.getId());
        if (!isOwner && !isRenter) {
            throw new ConflictException("Access denied");
        }

        contract.setStatus(ContractStatus.CLOSED);
        repository.save(contract);

        evidenceLogService.log(contract, EvidenceAction.CONTRACT_CLOSED);
    }

    // =========================
    // Queries
    // =========================
    public ContractResponseDto findById(Long id) {
        return toDto(getContract(id));
    }

    public List<ContractResponseDto> findMyContracts() {
        Long userId = getCurrentUser().getId();
        return repository.findByOwnerIdOrRenterId(userId, userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================
    // Helpers
    // =========================
    private void validateRenterIdentity(Contract contract, boolean requirePinflVerified) {
        User renter = contract.getRenter();

        if (renter.getPassportStatus() != VerificationStatus.VERIFIED) {
            throw new ConflictException("Renter passport is not verified");
        }

        if (renter.getPassportFrontPath() == null || renter.getPassportBackPath() == null) {
            throw new ValidationException("Renter passport images are required");
        }

        if (renter.getPinfl() == null || renter.getPinfl().isBlank()) {
            throw new ValidationException("Renter PINFL is required");
        }

        if (requirePinflVerified && renter.getPinflStatus() != VerificationStatus.VERIFIED) {
            throw new ConflictException("Renter PINFL is not verified");
        }
    }

    private Contract getContract(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException("Contract not found: " + id));
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ContractResponseDto toDto(Contract c) {
        ContractResponseDto dto = new ContractResponseDto();
        dto.setId(c.getId());
        dto.setOwnerId(c.getOwner().getId());
        dto.setRenterId(c.getRenter().getId());
        dto.setAssetId(c.getAsset().getId());
        dto.setBillingAmount(c.getBillingAmount());
        dto.setBillingUnit(c.getBillingUnit());
        dto.setPrepaidPeriods(c.getPrepaidPeriods());
        dto.setStatus(c.getStatus());
        dto.setStartAt(c.getStartAt());
        dto.setJurisdiction(c.getJurisdiction());
        dto.setLanguage(c.getLanguage());
        return dto;
    }
}
