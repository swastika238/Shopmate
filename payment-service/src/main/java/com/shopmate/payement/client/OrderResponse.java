package com.shopmate.payement.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderResponse(Long id, Long userId, String status, BigDecimal totalAmount){

    }

