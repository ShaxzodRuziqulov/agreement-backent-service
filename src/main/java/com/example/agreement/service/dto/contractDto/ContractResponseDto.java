package com.example.agreement.service.dto.contractDto;

import com.example.agreement.entity.enumerated.ContractStatus;
import com.example.agreement.entity.enumerated.PaymentPeriod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ContractResponseDto {

    private Long id;

    private Long assetId;
    private Long ownerId;
    private Long renterId;

    private BigDecimal paymentAmount;
    private PaymentPeriod paymentPeriod;
    private Integer paymentDay;

    private ContractStatus status;
    private LocalDateTime acceptedAt;

    private String jurisdiction;
    private String language;
}

