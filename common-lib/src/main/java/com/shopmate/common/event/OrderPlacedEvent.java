package com.shopmate.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPlacedEvent(
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        String status,
        LocalDateTime occurredAt
) {
}
