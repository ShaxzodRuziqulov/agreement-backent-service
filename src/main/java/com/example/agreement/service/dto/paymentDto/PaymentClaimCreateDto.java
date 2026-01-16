package com.example.agreement.service.dto.paymentDto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@Setter
public class PaymentClaimCreateDto {

    private YearMonth period;
    private BigDecimal amount;
}

