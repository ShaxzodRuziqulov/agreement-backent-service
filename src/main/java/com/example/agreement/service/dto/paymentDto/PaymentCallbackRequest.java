package com.example.agreement.service.dto.paymentDto;

import com.example.agreement.entity.enumerated.PaymentProvider;
import com.example.agreement.entity.enumerated.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Getter
@Setter
public class PaymentCallbackRequest {

    private Long paymentId;
    private PaymentProvider provider;
    private String providerTxnId;
    private PaymentStatus status;
    private BigDecimal amount;
}
