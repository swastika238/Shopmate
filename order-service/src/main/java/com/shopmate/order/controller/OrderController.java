package com.shopmate.order.controller;

import com.shopmate.order.dto.request.PlaceOrderRequest;
import com.shopmate.order.dto.response.OrderResponse;
import com.shopmate.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> place(@RequestHeader("X-User-Id") Long userId,
                                               @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(userId, request));
    }

    @GetMapping
    public List<OrderResponse> myOrders(@RequestHeader("X-User-Id") Long userId) {
        return orderService.getMyOrders(userId);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@RequestHeader("X-User-Id") Long userId, @PathVariable("id") Long id) {
        return orderService.getOrder(userId, id);
    }

    @PutMapping("/{id}/cancel")
    public OrderResponse cancel(@RequestHeader("X-User-Id") Long userId, @PathVariable("id") Long id) {
        return orderService.cancelOrder(userId, id);
    }
}
