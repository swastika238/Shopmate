package com.shopmate.order.service;

import com.shopmate.order.dto.request.PlaceOrderRequest;
import com.shopmate.order.dto.response.OrderResponse;
import com.shopmate.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Long userId, PlaceOrderRequest request);

    List<OrderResponse> getMyOrders(Long userId);

    OrderResponse getOrder(Long userId, Long orderId);

    OrderResponse cancelOrder(Long userId, Long orderId);

    OrderResponse getOrderInternal(Long orderId);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
}
