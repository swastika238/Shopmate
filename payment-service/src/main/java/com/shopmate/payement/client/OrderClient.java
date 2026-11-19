package com.shopmate.payement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name="order-service",url="${order-service.url}")
public interface OrderClient {

@GetMapping("/api/internal/orders/{id}")
        OrderResponse getOrder(@PathVariable("id")Long id);


        @PutMapping("/api/internal/orders/{id}/status")
        OrderResponse updateOrder(@PathVariable("id") Long id,
                                  @RequestBody OrderStatusUpdateRequest request);
    }

