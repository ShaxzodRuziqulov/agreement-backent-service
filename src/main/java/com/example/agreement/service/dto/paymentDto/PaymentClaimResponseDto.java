package com.example.agreement.service.dto.paymentDto;

import com.example.agreement.entity.enumerated.PaymentClaimStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Getter
@Setter
public class PaymentClaimResponseDto {

    private Long id;
    private YearMonth period;
    private BigDecimal amount;

    private PaymentClaimStatus status;
    private LocalDateTime confirmedAt;
}

