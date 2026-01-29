package com.example.agreement.service.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramContact {
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
}
