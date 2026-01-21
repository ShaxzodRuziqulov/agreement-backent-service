package com.example.agreement.service;

import com.example.agreement.entity.Contract;
import com.example.agreement.entity.EvidenceLog;
import com.example.agreement.entity.enumerated.EvidenceAction;
import com.example.agreement.repository.EvidenceLogRepository;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvidenceLogService {

    private final EvidenceLogRepository repository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @Transactional
    public void log(Contract contract, EvidenceAction action) {

        EvidenceLog log = new EvidenceLog();
        log.setContract(contract);
        log.setAction(action);

        Long userId = SecurityUtils.currentUserIdOrNull();
        if (userId != null) {
            userRepository.findById(userId).ifPresent(log::setActor);
        }

        log.setIpAddress(getClientIp());
        log.setUserAgent(request.getHeader("User-Agent"));

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("contractId", contract.getId());
        snapshot.put("status", contract.getStatus());
        snapshot.put("ownerId", contract.getOwner().getId());
        snapshot.put("renterId", contract.getRenter().getId());
        snapshot.put("assetId", contract.getAsset().getId());
        snapshot.put("paymentAmount", contract.getPaymentAmount());
        snapshot.put("paymentPeriod", contract.getPaymentPeriod());
        snapshot.put("paymentDay", contract.getPaymentDay());
        snapshot.put("acceptedAt", contract.getAcceptedAt());

        log.setPayloadSnapshot(snapshot);

        repository.save(log);
    }

    public List<EvidenceLog> findByContract(Long contractId) {
        return repository.findByContractIdOrderByCreatedAtDesc(contractId);
    }

    private String getClientIp() {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
