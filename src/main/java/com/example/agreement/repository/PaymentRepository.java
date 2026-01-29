package com.example.agreement.repository;

import com.example.agreement.entity.Payment;
import com.example.agreement.entity.enumerated.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByProviderTxnId(String providerTxnId);

    List<Payment> findByClaimIdOrderByCreatedAtDesc(Long claimId);

    List<Payment> findByPayerUserIdOrPayeeUserIdOrderByCreatedAtDesc(Long payerUserId, Long payeeUserId);

    @Query("""
           select coalesce(sum(p.amount), 0)
           from Payment p
           where p.claim.id = :claimId and p.status = :status
           """)
    BigDecimal sumAmountByClaimAndStatus(@Param("claimId") Long claimId,
                                         @Param("status") PaymentStatus status);
}
