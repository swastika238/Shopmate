package com.shopmate.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Subset of order-service's order JSON that payment-service needs. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderResponse(Long id, Long userId, String status, BigDecimal totalAmount) {
}
