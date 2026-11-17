package com.shopmate.order.service;

import com.shopmate.order.client.ProductClient;
import com.shopmate.order.client.ProductResponse;
import com.shopmate.order.dto.request.OrderItemRequest;
import com.shopmate.order.dto.request.PlaceOrderRequest;
import com.shopmate.order.dto.response.OrderResponse;
import com.shopmate.order.entity.Order;
import com.shopmate.order.entity.OrderStatus;
import com.shopmate.order.exception.InsufficientStockException;
import com.shopmate.order.mapper.OrderMapper;
import com.shopmate.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private ProductClient productClient;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productClient = mock(ProductClient.class);
        orderService = new OrderServiceImpl(orderRepository, productClient, new OrderMapper());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void placeOrder_success_calculatesTotalAndReducesStock() {
        when(productClient.getProduct(1L))
                .thenReturn(new ProductResponse(1L, "Laptop", new BigDecimal("500.00"), 10));

        OrderResponse response = orderService.placeOrder(7L,
                new PlaceOrderRequest(List.of(new OrderItemRequest(1L, 2))));

        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(new BigDecimal("1000.00"), response.totalAmount());
        assertEquals(1, response.items().size());
        verify(productClient).reduceStock(1L, 2);
    }

    @Test
    void placeOrder_insufficientStock_throwsAndDoesNotReduceStock() {
        when(productClient.getProduct(1L))
                .thenReturn(new ProductResponse(1L, "Laptop", new BigDecimal("500.00"), 1));

        assertThrows(InsufficientStockException.class, () ->
                orderService.placeOrder(7L, new PlaceOrderRequest(List.of(new OrderItemRequest(1L, 5)))));

        verify(productClient, never()).reduceStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }
}
