package com.example.agreement.service.dto.contractDto;

import com.example.agreement.entity.enumerated.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ContractActivateRequest {

    private PaymentMethod prepaidMethod = PaymentMethod.CASH;
    private BigDecimal prepaidAmount;
    private String handoverNote;
    private LocalDate periodStart;
}