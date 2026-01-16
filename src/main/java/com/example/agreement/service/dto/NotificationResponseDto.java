package com.example.agreement.service.dto;

import com.example.agreement.entity.enumerated.NotificationChannel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationResponseDto {

    private String message;
    private NotificationChannel channel;
    private boolean delivered;
    private LocalDateTime sentAt;
}

