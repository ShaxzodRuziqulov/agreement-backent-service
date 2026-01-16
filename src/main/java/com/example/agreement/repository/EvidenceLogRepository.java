package com.example.agreement.repository;

import com.example.agreement.entity.EvidenceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceLogRepository extends JpaRepository<EvidenceLog, Long> {

    List<EvidenceLog> findByContractIdOrderByCreatedAtDesc(Long contractId);
}
