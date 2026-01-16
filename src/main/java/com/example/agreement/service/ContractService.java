package com.example.agreement.service;

import com.example.agreement.entity.Asset;
import com.example.agreement.entity.Contract;
import com.example.agreement.entity.User;
import com.example.agreement.repository.ContractRepository;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.dto.contractDto.ContractCreateDto;
import com.example.agreement.service.dto.contractDto.ContractResponseDto;
import com.example.agreement.entity.enumerated.ContractStatus;
import com.example.agreement.entity.enumerated.EvidenceAction;
import com.example.agreement.entity.enumerated.VerificationStatus;
import com.example.agreement.util.SecurityUtils;
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
        Asset asset = assetService.getAsset(dto.getAssetId());

        Contract contract = new Contract();
        contract.setOwner(owner);

        User renter = userRepository.findById(dto.getRenterId())
                .orElseThrow(() -> new RuntimeException("Renter not found"));
        contract.setRenter(renter);

        contract.setAsset(asset);
        contract.setPaymentAmount(dto.getPaymentAmount());
        contract.setPaymentPeriod(dto.getPaymentPeriod());
        contract.setPaymentDay(dto.getPaymentDay());
        contract.setLanguage(dto.getLanguage());
        contract.setStatus(ContractStatus.DRAFT);

        Contract saved = repository.save(contract);

        evidenceLogService.log(saved, EvidenceAction.CONTRACT_CREATED);

        return toDto(saved);
    }

    @Transactional
    public void acceptContract(Long contractId) {
        User renter = getCurrentUser();
        Contract contract = getContract(contractId);

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT contracts can be accepted");
        }

        if (!contract.getRenter().getId().equals(renter.getId())) {
            throw new IllegalStateException("Only assigned renter can accept");
        }

        if (renter.getPinfl() == null) {
            throw new IllegalStateException("PINFL is required");
        }
        if (renter.getPassportStatus() != VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Passport is not verified");
        }

        contract.setStatus(ContractStatus.ACTIVE);
        contract.setAcceptedAt(LocalDateTime.now());

        repository.save(contract);

        evidenceLogService.log(contract, EvidenceAction.CONTRACT_ACCEPTED);
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
