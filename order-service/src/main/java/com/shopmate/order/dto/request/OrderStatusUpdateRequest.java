package com.shopmate.order.dto.request;

import com.shopmate.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "status is required") OrderStatus status) {
}
