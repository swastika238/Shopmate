package com.shopmate.order.service;

import com.shopmate.order.client.ProductClient;
import com.shopmate.order.client.ProductResponse;
import com.shopmate.order.dto.request.OrderItemRequest;
import com.shopmate.order.dto.request.PlaceOrderRequest;
import com.shopmate.order.dto.response.OrderResponse;
import com.shopmate.order.entity.Order;
import com.shopmate.order.entity.OrderItem;
import com.shopmate.order.entity.OrderStatus;
import com.shopmate.order.exception.InsufficientStockException;
import com.shopmate.order.exception.OrderNotFoundException;
import com.shopmate.order.mapper.OrderMapper;
import com.shopmate.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        // 1. Validate everything first
        for (OrderItemRequest req : request.items()) {
            ProductResponse product = productClient.getProduct(req.productId());
            if (product.stock() == null || product.stock() < req.quantity()) {
                throw new InsufficientStockException("Not enough stock for " + product.name());
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.id());
            item.setProductName(product.name());
            item.setPrice(product.price());
            item.setQuantity(req.quantity());
            item.setOrder(order);
            order.getItems().add(item);

            total = total.add(product.price().multiply(BigDecimal.valueOf(req.quantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        // 2. Reduce stock last
        for (OrderItem item : saved.getItems()) {
            productClient.reduceStock(item.getProductId(), item.getQuantity());
        }

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long orderId) {
        return orderMapper.toResponse(findOwned(userId, orderId));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = findOwned(userId, orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        // TODO: restore stock (add an increase-stock endpoint in product-service)
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private Order findOwned(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException(orderId); // don't reveal other users' orders
        }
        return order;
    }
}
