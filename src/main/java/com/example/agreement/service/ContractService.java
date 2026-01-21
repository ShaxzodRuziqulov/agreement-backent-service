package com.example.agreement.service;

import com.example.agreement.entity.Asset;
import com.example.agreement.entity.Contract;
import com.example.agreement.entity.User;
import com.example.agreement.entity.enumerated.ContractStatus;
import com.example.agreement.entity.enumerated.EvidenceAction;
import com.example.agreement.repository.ContractRepository;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.dto.contractDto.ContractCreateDto;
import com.example.agreement.service.dto.contractDto.ContractResponseDto;
import com.example.agreement.service.dto.userDto.PinflSubmitDto;
import com.example.agreement.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository repository;
    private final UserRepository userRepository;
    private final AssetService assetService;
    private final EvidenceLogService evidenceLogService;

    @Transactional
    public ContractResponseDto create(ContractCreateDto dto) {
        User owner = getCurrentUser();
        if (owner.getId().equals(dto.getRenterId())) {
            throw new IllegalArgumentException("Owner and renter cannot be the same user");
        }

        Asset asset = assetService.getAsset(dto.getAssetId());

        User renter = userRepository.findById(dto.getRenterId())
                .orElseThrow(() -> new EntityNotFoundException("Renter not found: " + dto.getRenterId()));

        Contract contract = new Contract();
        contract.setOwner(owner);
        contract.setRenter(renter);
        contract.setAsset(asset);
        contract.setPaymentAmount(dto.getPaymentAmount());
        contract.setPaymentPeriod(dto.getPaymentPeriod());
        contract.setPaymentDay(dto.getPaymentDay());
        contract.setLanguage(dto.getLanguage());
        contract.setStatus(ContractStatus.WAITING_RENTER_INFO);

        Contract saved = repository.save(contract);

        evidenceLogService.log(saved, EvidenceAction.CONTRACT_CREATED);

        return toDto(saved);
    }

    @Transactional
    public void closeContract(Long contractId) {
        User user = getCurrentUser();
        Contract contract = getContract(contractId);

        if (contract.getStatus() == ContractStatus.CLOSED) {
            throw new IllegalStateException("Contract already closed");
        }

        boolean isOwner = contract.getOwner().getId().equals(user.getId());
        boolean isRenter = contract.getRenter().getId().equals(user.getId());

        if (!isOwner && !isRenter) {
            throw new IllegalStateException("Access denied");
        }

        contract.setStatus(ContractStatus.CLOSED);
        repository.save(contract);

        evidenceLogService.log(contract, EvidenceAction.CONTRACT_CLOSED);
    }

    @Transactional
    public void renterSubmit(Long contractId, PinflSubmitDto request) {

        User renter = getCurrentUser();
        Contract contract = getContract(contractId);

        if (!contract.getRenter().getId().equals(renter.getId())) {
            throw new IllegalStateException("Only assigned renter can submit");
        }

        if (contract.getStatus() != ContractStatus.WAITING_RENTER_INFO) {
            throw new IllegalStateException("Contract is not waiting for renter info");
        }

        if (renter.getPassportFrontPath() == null || renter.getPassportBackPath() == null) {
            throw new IllegalStateException("Passport images are required");
        }

        if (request == null || request.getPinfl() == null || request.getPinfl().isBlank()) {
            throw new IllegalStateException("PINFL is required");
        }

        String pinfl = request.getPinfl().trim();
        if (!pinfl.matches("\\d{14}")) {
            throw new IllegalStateException("PINFL must be 14 digits");
        }

        if (renter.getPinfl() == null || renter.getPinfl().isBlank()) {
            if (userRepository.existsByPinfl(pinfl)) {
                throw new IllegalStateException("PINFL already used by another user");
            }
            renter.setPinfl(pinfl);
            userRepository.save(renter);
        }

        contract.setStatus(ContractStatus.WAITING_OWNER_APPROVAL);
        repository.save(contract);

        evidenceLogService.log(contract, EvidenceAction.RENTER_SUBMITTED_INFO);
    }

    @Transactional
    public void ownerApprove(Long contractId) {

        User owner = getCurrentUser();
        Contract contract = getContract(contractId);

        if (!contract.getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("Only owner can approve");
        }

        if (contract.getStatus() != ContractStatus.WAITING_OWNER_APPROVAL) {
            throw new IllegalStateException("Contract is not waiting for owner approval");
        }

        User renter = contract.getRenter();

        if (renter.getPassportFrontPath() == null || renter.getPassportBackPath() == null) {
            throw new IllegalStateException("Renter passport images are missing");
        }

        if (renter.getPinfl() ==null || renter.getPinfl().isBlank()) {
            throw new IllegalStateException("Renter pinfl is missing");
        }

        contract.setStatus(ContractStatus.ACTIVE);
        contract.setAcceptedAt(LocalDateTime.now());

        repository.save(contract);
        evidenceLogService.log(contract, EvidenceAction.OWNER_APPROVED_CONTRACT);
    }

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

    private Contract getContract(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
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
        dto.setPaymentAmount(c.getPaymentAmount());
        dto.setPaymentPeriod(c.getPaymentPeriod());
        dto.setPaymentDay(c.getPaymentDay());
        dto.setStatus(c.getStatus());
        dto.setAcceptedAt(c.getAcceptedAt());
        dto.setJurisdiction(c.getJurisdiction());
        dto.setLanguage(c.getLanguage());
        return dto;
    }
}
