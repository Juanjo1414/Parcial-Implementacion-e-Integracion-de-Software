package com.eia.camelracing.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {
    public ErrorResponse(String message, int status, LocalDateTime timestamp) {
        this(message, status, timestamp, null);
    }
}