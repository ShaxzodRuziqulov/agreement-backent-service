package com.example.agreement.service.dto.paymentDto;

import com.example.agreement.entity.enumerated.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Getter
@Setter
public class PaymentDto {
    private Long id;
    private Long contractId;
    private BigDecimal amount;
    private YearMonth period;
    private PaymentStatus status;
    private LocalDateTime confirmedAt;
}

