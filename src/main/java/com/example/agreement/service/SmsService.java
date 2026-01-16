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

    @Value("${security.sms.api-token}")
    private String apiToken;

    public void sendOtp(String phoneNumber, String code) {
        try {
            String message = String.format(
                    "Sizning tasdiqlash kodingiz: %s\nKodni hech kimga bermang!",
                    code
            );

            if ("eskiz".equalsIgnoreCase(provider)) {
                sendViaEskiz(phoneNumber, message);
            } else {
                // Fallback or other providers
                log.warn("SMS provider not configured. OTP: {} for {}", code, phoneNumber);
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            // Don't throw exception - allow OTP to work even if SMS fails
        }
    }

    private void sendViaEskiz(String phoneNumber, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);

            Map<String, String> request = new HashMap<>();
            request.put("mobile_phone", normalizePhoneNumber(phoneNumber));
            request.put("message", message);
            request.put("from", "4546");

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/message/sms/send",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent successfully to {}", phoneNumber);
            } else {
                log.error("Failed to send SMS. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending SMS via Eskiz: {}", e.getMessage());
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        // Remove all non-digit characters
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        // If starts with 998, return as is
        if (cleaned.startsWith("998")) {
            return cleaned;
        }

        // If starts with 0, replace with 998
        if (cleaned.startsWith("0")) {
            return "998" + cleaned.substring(1);
        }

        // Otherwise, add 998
        return "998" + cleaned;
    }
}


