package com.shopmate.payement.dto.request;
import com.shopmate.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * simulateFailure is only for testing the FAKE payment flow (Stage A).
 * Remove it when you integrate a real gateway like Khalti or eSewa.
 */
public record PaymentRequest(
        @NotNull(message = "orderId is required") Long orderId,
        @NotNull(message = "method is required") PaymentMethod method,
        Boolean simulateFailure) {
}
