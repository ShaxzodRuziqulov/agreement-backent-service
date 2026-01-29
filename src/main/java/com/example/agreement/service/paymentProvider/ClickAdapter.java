package com.example.agreement.service.paymentProvider;

import com.example.agreement.entity.Payment;
import com.example.agreement.entity.enumerated.PaymentProvider;
import com.example.agreement.service.dto.paymentDto.PaymentCallbackRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClickAdapter extends BaseSecretAdapter implements PaymentProviderAdapter {

    @Value("${app.payments.click.redirect-base:https://payments.local/click}")
    private String redirectBase;

    @Value("${app.payments.click.secret:}")
    private String secret;

    @Override public PaymentProvider provider() { return PaymentProvider.CLICK; }

    @Override
    public String buildRedirectUrl(Payment payment) {
        return redirectBase + "/redirect?paymentId=" + payment.getId();
    }

    @Override
    public VerifiedCallback verifyCallback(PaymentCallbackRequest req, String providedSecret) {
        checkSecret(secret, providedSecret, "CLICK");
        return basicVerified(req);
    }
}

