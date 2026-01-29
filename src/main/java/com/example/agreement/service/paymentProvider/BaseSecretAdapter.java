package com.example.agreement.service.paymentProvider;

import com.example.agreement.entity.enumerated.PaymentStatus;
import com.example.agreement.exeption.ConflictException;
import com.example.agreement.service.dto.paymentDto.PaymentCallbackRequest;

public abstract class BaseSecretAdapter {

    protected void checkSecret(String expected, String provided, String providerName) {
        if (expected != null && !expected.isBlank()) {
            if (!expected.equals(provided)) {
                throw new ConflictException("Invalid " + providerName + " callback secret");
            }
        }
    }

    protected PaymentProviderAdapter.VerifiedCallback basicVerified(PaymentCallbackRequest req) {
        PaymentProviderAdapter.VerifiedCallback v = new PaymentProviderAdapter.VerifiedCallback();
        v.status = (req.getStatus() == null) ? PaymentStatus.FAILED : req.getStatus();
        v.providerTxnId = req.getProviderTxnId();
        v.amount = req.getAmount();
        return v;
    }
}
