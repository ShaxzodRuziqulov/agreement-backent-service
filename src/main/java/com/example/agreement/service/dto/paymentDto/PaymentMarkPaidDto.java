package com.example.agreement.service.dto.paymentDto;

import com.example.agreement.entity.enumerated.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMarkPaidDto {

    private PaymentMethod method;
}
