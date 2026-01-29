package com.example.agreement.service.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramMessage {
    @JsonProperty("message_id")
    private Long messageId;

    private TelegramUser from;
    private TelegramChat chat;
    private String text;
    private TelegramContact contact;
}
