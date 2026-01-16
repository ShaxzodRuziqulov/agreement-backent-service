package com.example.agreement.exeption;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiErrorResponse {

    private String message;
    private String error;
    private int status;
    private String timestamp;
}
