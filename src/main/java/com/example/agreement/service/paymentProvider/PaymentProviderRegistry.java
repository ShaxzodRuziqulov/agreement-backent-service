package com.example.agreement.service.paymentProvider;

import com.example.agreement.entity.enumerated.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProviderRegistry {

    private final Map<PaymentProvider, PaymentProviderAdapter> map = new EnumMap<>(PaymentProvider.class);

    public PaymentProviderRegistry(List<PaymentProviderAdapter> adapters) {
        for (PaymentProviderAdapter a : adapters) {
            map.put(a.provider(), a);
        }
    }

    public PaymentProviderAdapter get(PaymentProvider provider) {
        PaymentProviderAdapter a = map.get(provider);
        if (a == null) throw new IllegalStateException("Provider not supported: " + provider);
        return a;
    }
}
