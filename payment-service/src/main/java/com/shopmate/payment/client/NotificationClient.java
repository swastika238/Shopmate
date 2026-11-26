package com.shopmate.payment.client;

import com.shopmate.common.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification-service.url}")
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    void send(@RequestBody NotificationRequest request);
}
