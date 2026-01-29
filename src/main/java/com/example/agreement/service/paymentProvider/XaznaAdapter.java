package com.example.agreement.service.paymentProvider;

import com.example.agreement.entity.Payment;
import com.example.agreement.entity.enumerated.PaymentProvider;
import com.example.agreement.service.dto.paymentDto.PaymentCallbackRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class XaznaAdapter extends BaseSecretAdapter implements PaymentProviderAdapter {

    @Value("${app.payments.xazna.redirect-base:https://payments.local/xazna}")
    private String redirectBase;

    @Value("${app.payments.xazna.secret:}")
    private String secret;

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.XAZNA;
    }

    @Override
    public String buildRedirectUrl(Payment payment) {
        return redirectBase + "/redirect?paymentId=" + payment.getId();
    }

    @Override
    public VerifiedCallback verifyCallback(PaymentCallbackRequest req, String providedSecret) {
        checkSecret(secret, providedSecret, "XAZNA");
        return basicVerified(req);
    }
}

