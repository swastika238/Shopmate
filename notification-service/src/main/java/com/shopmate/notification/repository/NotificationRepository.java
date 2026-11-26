package com.shopmate.notification.repository;

import com.shopmate.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByOrderIdAndUserIdOrderByCreatedAtDesc(Long orderId, Long userId);
}
