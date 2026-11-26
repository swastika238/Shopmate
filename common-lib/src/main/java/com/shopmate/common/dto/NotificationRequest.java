package com.shopmate.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull(message = "userId is required") Long userId,
        Long orderId,
        @NotBlank(message = "type is required") String type,
        String channel,
        String recipient,
        @NotBlank(message = "subject is required") String subject,
        @NotBlank(message = "message is required") String message
) {
}
