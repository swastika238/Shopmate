package com.shopmate.notification.dto;

import com.shopmate.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        Long orderId,
        String type,
        String channel,
        String recipient,
        String subject,
        String message,
        String status,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUserId(),
                n.getOrderId(),
                n.getType(),
                n.getChannel(),
                n.getRecipient(),
                n.getSubject(),
                n.getMessage(),
                n.getStatus(),
                n.getCreatedAt()
        );
    }
}
