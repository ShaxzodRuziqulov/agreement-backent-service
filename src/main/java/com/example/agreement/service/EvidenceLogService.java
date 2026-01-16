package com.example.agreement.service;

import com.example.agreement.entity.Contract;
import com.example.agreement.entity.EvidenceLog;
import com.example.agreement.repository.EvidenceLogRepository;
import com.example.agreement.entity.enumerated.EvidenceAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvidenceLogService {

    private final EvidenceLogRepository repository;

    public void log(Contract contract, EvidenceAction action) {
        EvidenceLog log = new EvidenceLog();
        log.setContract(contract);
        log.setAction(action);
        repository.save(log);
    }

    public List<EvidenceLog> findByContract(Long contractId) {
        return repository.findByContractIdOrderByCreatedAtDesc(contractId);
    }
}
