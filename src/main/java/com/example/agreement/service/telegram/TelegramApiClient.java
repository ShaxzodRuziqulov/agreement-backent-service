package com.example.agreement.service.telegram;

import com.example.agreement.service.telegram.dto.TelegramUpdate;
import com.example.agreement.service.telegram.dto.TelegramUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramApiClient {

    private final RestTemplate restTemplate;

    @Value("${telegram.bot.enabled:false}")
    private boolean enabled;

    @Value("${telegram.bot.token:}")
    private String token;

    @Value("${telegram.bot.api-url:https://api.telegram.org}")
    private String apiUrl;

    public boolean isEnabled() {
        return enabled && token != null && !token.isBlank();
    }

    public List<TelegramUpdate> getUpdates(long offset, int timeoutSeconds) {
        if (!isEnabled()) {
            return Collections.emptyList();
        }

        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .pathSegment("bot" + token, "getUpdates")
                .queryParam("timeout", timeoutSeconds)
                .queryParam("offset", offset)
                .toUriString();

        try {
            TelegramUpdateResponse response =
                    restTemplate.getForObject(url, TelegramUpdateResponse.class);

            if (response == null || response.getResult() == null) {
                return Collections.emptyList();
            }

            return response.getResult();

        } catch (Exception e) {
            log.error("Telegram getUpdates failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }


    public void sendMessage(Long chatId, String text, Map<String, Object> replyMarkup) {
        if (!isEnabled()) {
            return;
        }

        String url = apiUrl + "/bot" + token + "/sendMessage";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        if (replyMarkup != null) {
            body.put("reply_markup", replyMarkup);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            log.error("Telegram sendMessage failed: {}", e.getMessage());
        }
    }

    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }
}
