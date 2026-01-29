package com.example.agreement.service.dto.contractDto;

import com.example.agreement.entity.enumerated.BillingUnit;
import com.example.agreement.entity.enumerated.PaymentPeriod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ContractCreateDto {

    private Long assetId;

    private BigDecimal billingAmount;
    private BillingUnit billingUnit;
    private Integer prepaidPeriods;

    private String language;
    private Long renterId;
}

