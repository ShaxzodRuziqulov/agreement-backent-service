package com.example.agreement.service.admin;

import com.example.agreement.entity.Contract;
import com.example.agreement.repository.ContractRepository;
import com.example.agreement.service.EvidenceLogService;
import com.example.agreement.entity.enumerated.ContractStatus;
import com.example.agreement.entity.enumerated.EvidenceAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminContractService {

    private final ContractRepository repository;
    private final EvidenceLogService evidenceLogService;

    @Transactional
    public void suspendContract(Long contractId, String reason) {
        Contract contract = repository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        contract.setStatus(ContractStatus.SUSPENDED);
        repository.save(contract);

        evidenceLogService.log(
                contract,
                EvidenceAction.CONTRACT_SUSPENDED
        );
    }
}
