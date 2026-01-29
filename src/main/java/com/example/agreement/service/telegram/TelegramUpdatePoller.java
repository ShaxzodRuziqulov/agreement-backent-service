package com.example.agreement.service.telegram;

import com.example.agreement.service.telegram.dto.TelegramUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramUpdatePoller {

    private final TelegramApiClient apiClient;
    private final TelegramBotHandler botHandler;

    private final AtomicLong offset = new AtomicLong(0);

    @Value("${telegram.bot.poll-timeout-seconds:20}")
    private int pollTimeoutSeconds;

    @Scheduled(fixedDelayString = "${telegram.bot.poll-interval-ms:2000}")
    public void poll() {
        if (!apiClient.isEnabled()) {
            return;
        }

        List<TelegramUpdate> updates = apiClient.getUpdates(offset.get(), pollTimeoutSeconds);
        for (TelegramUpdate update : updates) {
            if (update.getUpdateId() != null) {
                offset.set(update.getUpdateId() + 1);
            }
            botHandler.handleUpdate(update);
        }
    }
}
