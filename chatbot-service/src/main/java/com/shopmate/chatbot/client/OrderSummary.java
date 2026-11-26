package com.shopmate.chatbot.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderSummary(
        Long id,
        Long userId,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
