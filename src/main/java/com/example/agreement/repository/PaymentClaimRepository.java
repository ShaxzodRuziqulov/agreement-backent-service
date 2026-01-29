package com.example.agreement.repository;

import com.example.agreement.entity.PaymentClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Repository
public interface PaymentClaimRepository extends JpaRepository<PaymentClaim, Long> {
    List<PaymentClaim> findByContractIdOrderByPeriodStartDesc(Long contractId);

    boolean existsByContractIdAndPeriodStart(Long contractId, LocalDate periodStart);}
