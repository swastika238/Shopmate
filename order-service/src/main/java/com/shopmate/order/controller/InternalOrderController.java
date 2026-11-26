package com.shopmate.order.controller;

import com.shopmate.common.constants.SecurityConstants;
import com.shopmate.order.dto.request.OrderStatusUpdateRequest;
import com.shopmate.order.dto.response.OrderResponse;
import com.shopmate.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @Value("${internal.api-key}")
    private String internalApiKey;

    @GetMapping("/{id}")
    public OrderResponse getOrder(@RequestHeader(SecurityConstants.HEADER_INTERNAL_KEY) String key,
                                  @PathVariable("id") Long id) {
        verifyKey(key);
        return orderService.getOrderInternal(id);
    }

    @PutMapping("/{id}/status")
    public OrderResponse updateStatus(@RequestHeader(SecurityConstants.HEADER_INTERNAL_KEY) String key,
                                      @PathVariable("id") Long id,
                                      @Valid @RequestBody OrderStatusUpdateRequest request) {
        verifyKey(key);
        return orderService.updateOrderStatus(id, request.status());
    }

    private void verifyKey(String providedKey) {
        if (!internalApiKey.equals(providedKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal API key");
        }
    }
}
