package com.shopmate.notification.controller;

import com.shopmate.common.dto.NotificationRequest;
import com.shopmate.notification.dto.NotificationResponse;
import com.shopmate.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.send(request));
    }

    @GetMapping
    public List<NotificationResponse> myNotifications(@RequestHeader("X-User-Id") Long userId) {
        return notificationService.getMyNotifications(userId);
    }

    @GetMapping("/{id}")
    public NotificationResponse get(@RequestHeader("X-User-Id") Long userId, @PathVariable("id") Long id) {
        return notificationService.getNotification(userId, id);
    }

    @GetMapping("/order/{orderId}")
    public List<NotificationResponse> forOrder(@RequestHeader("X-User-Id") Long userId,
                                               @PathVariable("orderId") Long orderId) {
        return notificationService.getByOrder(userId, orderId);
    }
}
