package com.example.agreement.service.dto.paymentDto;

import com.example.agreement.entity.enumerated.PaymentMethod;
import com.example.agreement.entity.enumerated.PaymentProvider;
import com.example.agreement.entity.enumerated.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponseDto {

    private Long id;
    private Long contractId;
    private Long claimId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private PaymentProvider provider;
    private String providerTxnId;
    private Long createdBy;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private String redirectUrl;
}