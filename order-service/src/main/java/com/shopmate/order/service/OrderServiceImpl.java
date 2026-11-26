package com.shopmate.order.service;

import com.shopmate.common.dto.NotificationRequest;
import com.shopmate.order.client.NotificationClient;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;
    private final NotificationClient notificationClient;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductClient productClient,
                            OrderMapper orderMapper) {
        this(orderRepository, productClient, orderMapper, null);
    }

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductClient productClient,
                            OrderMapper orderMapper,
                            NotificationClient notificationClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.orderMapper = orderMapper;
        this.notificationClient = notificationClient;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

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

        for (OrderItem item : saved.getItems()) {
            productClient.reduceStock(item.getProductId(), item.getQuantity());
        }

        sendNotification(saved.getUserId(), saved.getId(), "ORDER_PLACED",
                "Order #" + saved.getId() + " Placed",
                "Your order #" + saved.getId() + " for $" + saved.getTotalAmount() + " has been placed and is awaiting payment.");

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
        Order saved = orderRepository.save(order);

        for (OrderItem item : saved.getItems()) {
            try {
                productClient.restoreStock(item.getProductId(), item.getQuantity());
            } catch (Exception ex) {
                log.warn("Failed to restore stock for product {}: {}", item.getProductId(), ex.getMessage());
            }
        }

        sendNotification(saved.getUserId(), saved.getId(), "ORDER_CANCELLED",
                "Order #" + saved.getId() + " Cancelled",
                "Your order #" + saved.getId() + " has been cancelled.");

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderInternal(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(status);
        Order saved = orderRepository.save(order);

        sendNotification(saved.getUserId(), saved.getId(), "ORDER_STATUS_UPDATED",
                "Order #" + saved.getId() + " " + status,
                "Your order #" + saved.getId() + " status is now " + status + ".");

        return orderMapper.toResponse(saved);
    }

    private Order findOwned(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }
        return order;
    }

    private void sendNotification(Long userId, Long orderId, String type, String subject, String message) {
        if (notificationClient == null) {
            return;
        }
        try {
            notificationClient.send(new NotificationRequest(
                    userId, orderId, type, "IN_APP", null, subject, message));
        } catch (Exception ex) {
            log.warn("Failed to send {} notification for order {}: {}", type, orderId, ex.getMessage());
        }
    }
}
