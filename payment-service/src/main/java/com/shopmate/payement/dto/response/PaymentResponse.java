package com.shopmate.payment.dto.response;

import com.shopmate.payment.entity.Payment;
import com.shopmate.payment.entity.PaymentMethod;
import com.shopmate.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long userId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String transactionId,
        String failureReason,
        LocalDateTime createdAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getOrderId(), p.getUserId(), p.getAmount(), p.getMethod(),
                p.getStatus(), p.getTransactionId(), p.getFailureReason(), p.getCreatedAt());
    }
}
