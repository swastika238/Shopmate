package com.shopmate.notification.service;

import com.shopmate.common.dto.NotificationRequest;
import com.shopmate.notification.dto.NotificationResponse;
import com.shopmate.notification.entity.Notification;
import com.shopmate.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationRepository repository;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRepository.class);
        service = new NotificationService(repository);
        when(repository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
    }

    @Test
    void send_validRequest_savesAndReturnsSent() {
        NotificationRequest req = new NotificationRequest(
                1L, 10L, "ORDER_PLACED", "email", "u@test.com", "Order Placed", "Thanks!");

        NotificationResponse res = service.send(req);

        assertEquals(1L, res.id());
        assertEquals("EMAIL", res.channel());
        assertEquals("SENT", res.status());
    }
}
