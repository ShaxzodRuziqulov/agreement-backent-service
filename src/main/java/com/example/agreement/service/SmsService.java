package com.example.agreement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final RestTemplate restTemplate;

    @Value("${security.sms.provider}")
    private String provider;

    @Value("${security.sms.api-url}")
    private String apiUrl;

    @Value("${security.sms.eskiz-email:}")
    private String eskizEmail;

    @Value("${security.sms.eskiz-password:}")
    private String eskizPassword;

    @Value("${security.sms.eskiz-from:4546}")
    private String eskizFrom;

    public void sendOtp(String phoneNumber, String code) {
        String message = String.format(
                "Sizning tasdiqlash kodingiz: %s\nKodni hech kimga bermang!",
                code
        );

        if ("dev".equalsIgnoreCase(provider)) {
            log.info("✅ DEV OTP for {} = {}", phoneNumber, code);
            return;
        }

        if ("eskiz".equalsIgnoreCase(provider)) {
            sendViaEskiz(phoneNumber, message);
            return;
        }

        log.warn("❗ Unknown SMS provider: {}. OTP for {} = {}", provider, phoneNumber, code);
    }

    private void sendViaEskiz(String phoneNumber, String message) {
        try {
            String token = loginEskizAndGetToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, Object> request = new HashMap<>();
            request.put("mobile_phone", normalizePhoneNumber(phoneNumber));
            request.put("message", message);
            request.put("from", eskizFrom);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/message/sms/send",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Eskiz SMS status={} body={}", response.getStatusCode(), response.getBody());

        } catch (Exception e) {
            log.error("❌ Error sending SMS via Eskiz: {}", e.getMessage());
        }
    }

    private String loginEskizAndGetToken() {
        if (eskizEmail == null || eskizEmail.isBlank() || eskizPassword == null || eskizPassword.isBlank()) {
            throw new RuntimeException("Eskiz email/password not configured in application.yml");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("email", eskizEmail);
        body.put("password", eskizPassword);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl + "/auth/login",
                entity,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Eskiz login failed: " + response.getStatusCode());
        }

        Object dataObj = response.getBody().get("data");
        if (!(dataObj instanceof Map)) {
            throw new RuntimeException("Eskiz login response invalid: data not found");
        }

        Map data = (Map) dataObj;
        Object tokenObj = data.get("token");
        if (tokenObj == null) {
            throw new RuntimeException("Eskiz token not found in response");
        }

        return tokenObj.toString();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("998")) return cleaned;
        if (cleaned.startsWith("0")) return "998" + cleaned.substring(1);
        return "998" + cleaned;
    }
}
