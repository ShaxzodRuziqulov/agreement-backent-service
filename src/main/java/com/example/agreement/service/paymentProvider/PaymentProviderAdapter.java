package com.example.agreement.service.paymentProvider;

import com.example.agreement.entity.Payment;
import com.example.agreement.entity.enumerated.PaymentProvider;
import com.example.agreement.entity.enumerated.PaymentStatus;
import com.example.agreement.service.dto.paymentDto.PaymentCallbackRequest;

import java.math.BigDecimal;

public interface PaymentProviderAdapter {

    PaymentProvider provider();

    String buildRedirectUrl(Payment payment);

    VerifiedCallback verifyCallback(PaymentCallbackRequest req, String secret);

    class VerifiedCallback {
        public PaymentStatus status;
        public String providerTxnId;
        public BigDecimal amount;
    }
}
