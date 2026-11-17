package com.shopmate.order.event;

import java.math.BigDecimal;

/** Will be published to Kafka/RabbitMQ later for notification-service. */
public record OrderPlacedEvent(Long orderId, Long userId, BigDecimal totalAmount) {
}
