package com.example.agreement.repository;

import com.example.agreement.entity.OtpCode;
import com.example.agreement.entity.enumerated.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SmsOtpRepository extends JpaRepository<OtpCode, Long> {

    @Query("""
                SELECT o FROM OtpCode o
                WHERE o.phoneNumber = :phoneNumber
                  AND o.type = :type
                  AND o.used = false
                  AND o.expiresAt > CURRENT_TIMESTAMP
                ORDER BY o.createdAt DESC
            """)
    Optional<OtpCode> findLatestValidOtp(
            @Param("phoneNumber") String phoneNumber,
            @Param("type") OtpType type
    );

    @Modifying
    @Transactional
    @Query("""
                UPDATE OtpCode o
                SET o.used = true
                WHERE o.phoneNumber = :phoneNumber
                  AND o.type = :type
            """)
    void invalidatePreviousOtps(
            @Param("phoneNumber") String phoneNumber,
            @Param("type") OtpType type
    );

    @Modifying
    @Transactional
    @Query("""
                DELETE FROM OtpCode o
                WHERE o.expiresAt < :now
            """)
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
}
