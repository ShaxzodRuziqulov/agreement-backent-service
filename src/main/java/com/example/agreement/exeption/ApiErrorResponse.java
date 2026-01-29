package com.example.agreement.exeption;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private String message;      // user-friendly
    private String error;        // HTTP reason
    private int status;

    private String type;         // exception class (short)
    private String code;         // app code (optional)
    private String path;         // request uri
    private String traceId;      // correlation id

    private OffsetDateTime timestamp;

    private Map<String, Object> details; // constraint, sqlState, fieldErrors, etc.
}
