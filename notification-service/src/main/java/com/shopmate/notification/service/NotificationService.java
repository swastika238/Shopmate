package com.shopmate.notification.service;

import com.shopmate.common.dto.NotificationRequest;
import com.shopmate.notification.dto.NotificationResponse;
import com.shopmate.notification.entity.Notification;
import com.shopmate.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationResponse send(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.userId());
        notification.setOrderId(request.orderId());
        notification.setType(request.type());
        notification.setChannel(request.channel() != null && !request.channel().isBlank()
                ? request.channel().toUpperCase()
                : "IN_APP");
        notification.setRecipient(request.recipient());
        notification.setSubject(request.subject());
        notification.setMessage(request.message());
        notification.setStatus("SENT");

        Notification saved = notificationRepository.save(notification);
        log.info("Notification [{}] sent via {} to userId={}: {}",
                saved.getType(), saved.getChannel(), saved.getUserId(), saved.getSubject());
        return NotificationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long userId, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Notification not found: " + id);
        }
        return NotificationResponse.from(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByOrder(Long userId, Long orderId) {
        return notificationRepository.findByOrderIdAndUserIdOrderByCreatedAtDesc(orderId, userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
