package com.shopmate.common.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public ApiError(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now(), null);
    }
}
