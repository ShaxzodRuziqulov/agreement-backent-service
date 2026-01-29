package com.example.agreement.service.telegram;

import com.example.agreement.entity.User;
import com.example.agreement.service.dto.contractDto.ContractResponseDto;
import com.example.agreement.service.dto.paymentDto.PaymentResponseDto;
import com.example.agreement.service.dto.assetDto.AssetResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotBackendClient {

    private final RestTemplate restTemplate;

    @Value("${telegram.backend-url:http://localhost:8081}")
    private String baseUrl;

    public User getCurrentUser(String token) {
        return exchange(
                "/api/v1/users/me",
                HttpMethod.GET,
                token,
                null,
                User.class
        );
    }

    public List<ContractResponseDto> getMyContracts(String token) {
        return exchange(
                "/api/v1/contracts/my",
                HttpMethod.GET,
                token,
                null,
                new ParameterizedTypeReference<List<ContractResponseDto>>() {}
        );
    }

    public ContractResponseDto getContract(String token, Long contractId) {
        return exchange(
                "/api/v1/contracts/" + contractId,
                HttpMethod.GET,
                token,
                null,
                ContractResponseDto.class
        );
    }

    public List<AssetResponseDto> getMyAssets(String token) {
        return exchange(
                "/api/assets/my",
                HttpMethod.GET,
                token,
                null,
                new ParameterizedTypeReference<List<AssetResponseDto>>() {}
        );
    }


    public PaymentResponseDto getPayment(String token, Long paymentId) {
        return exchange(
                "/api/payments/" + paymentId,
                HttpMethod.GET,
                token,
                null,
                PaymentResponseDto.class
        );
    }

    public List<PaymentResponseDto> getMyPayments(String token) {
        return exchange(
                "/api/payments/my",
                HttpMethod.GET,
                token,
                null,
                new ParameterizedTypeReference<List<PaymentResponseDto>>() {}
        );
    }

    private <T> T exchange(String path,
                           HttpMethod method,
                           String token,
                           Object body,
                           Class<T> responseType) {
        HttpEntity<?> entity = new HttpEntity<>(body, authHeaders(token));
        ResponseEntity<T> response = restTemplate.exchange(
                baseUrl + path,
                method,
                entity,
                responseType
        );
        return response.getBody();
    }

    private <T> T exchange(String path,
                           HttpMethod method,
                           String token,
                           Object body,
                           ParameterizedTypeReference<T> responseType) {
        HttpEntity<?> entity = new HttpEntity<>(body, authHeaders(token));
        ResponseEntity<T> response = restTemplate.exchange(
                baseUrl + path,
                method,
                entity,
                responseType
        );
        return response.getBody();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }
        return headers;
    }
}
