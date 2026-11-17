package com.shopmate.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Shape of the product returned by product-service.
 * Field names must match product-service's JSON (id, name, price, stock).
 * If yours differ, rename here or use @JsonProperty("yourFieldName").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(Long id, String name, BigDecimal price, Integer stock) {
}
