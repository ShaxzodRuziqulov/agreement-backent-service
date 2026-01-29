package com.example.agreement.service.dto.contractDto;

import com.example.agreement.entity.enumerated.BillingUnit;
import com.example.agreement.entity.enumerated.ContractStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ContractDto {
    private Long id;

    private Long ownerId;
    private Long renterId;
    private Long assetId;

    private BigDecimal billingAmount;
    private BillingUnit billingUnit;
    private Integer prepaidPeriods;

    private ContractStatus status;
    private LocalDateTime startAt;
    private LocalDateTime createdAt;
}