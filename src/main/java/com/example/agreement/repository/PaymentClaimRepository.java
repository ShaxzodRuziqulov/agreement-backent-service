package com.example.agreement.repository;

import com.example.agreement.entity.PaymentClaim;
import com.example.agreement.entity.enumerated.PaymentClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;

@Repository
public interface PaymentClaimRepository extends JpaRepository<PaymentClaim, Long> {
    List<PaymentClaim> findByContractIdOrderByPeriodDesc(Long contractId);

    boolean existsByContractIdAndPeriod(Long contractId, YearMonth period);
}
