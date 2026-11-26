package com.shopmate.chatbot.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "order-service", url = "${order-service.url}")
public interface OrderClient {

    @GetMapping("/api/orders")
    List<OrderSummary> getMyOrders(@RequestHeader("X-User-Id") Long userId);

    @GetMapping("/api/orders/{id}")
    OrderSummary getOrder(@RequestHeader("X-User-Id") Long userId, @PathVariable("id") Long id);
}
