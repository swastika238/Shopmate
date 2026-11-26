package com.shopmate.chatbot.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductSearchRequest(
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String category,
        List<String> features,
        String keyword
) {
}
