package com.example.agreement.service.dto.contractDto;

import com.example.agreement.entity.enumerated.BillingUnit;
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

    private BigDecimal billingAmount;
    private BillingUnit billingUnit;
    private Integer prepaidPeriods;

    private ContractStatus status;
    private LocalDateTime startAt;

    private String jurisdiction;
    private String language;
}

