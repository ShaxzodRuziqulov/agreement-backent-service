package com.example.agreement.service.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TelegramUpdateResponse {
    private boolean ok;

    @JsonProperty("result")
    private List<TelegramUpdate> result;
}
