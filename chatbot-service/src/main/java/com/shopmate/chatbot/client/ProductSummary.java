package com.shopmate.chatbot.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSummary(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        BigDecimal rating,
        String category,
        Set<String> features
) {
}
