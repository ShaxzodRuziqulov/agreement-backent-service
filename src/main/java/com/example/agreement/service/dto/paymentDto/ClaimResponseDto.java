package com.example.agreement.service.dto.paymentDto;

import com.example.agreement.entity.enumerated.PaymentClaimStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
    public class ClaimResponseDto {

        private Long id;
        private Long contractId;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private BigDecimal amount;

        private BigDecimal paidAmount;
        private PaymentClaimStatus status;
        private LocalDateTime dueAt;
        private List<PaymentResponseDto> payments;
    }
