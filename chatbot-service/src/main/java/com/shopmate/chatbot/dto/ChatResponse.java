package com.shopmate.chatbot.dto;

import com.shopmate.chatbot.client.OrderSummary;
import com.shopmate.chatbot.client.ProductSummary;

import java.util.List;

public record ChatResponse(
        String reply,
        String intent,
        ProductSearchRequest parsedFilters,
        List<ProductSummary> recommendedProducts,
        List<OrderSummary> orders
) {
}
