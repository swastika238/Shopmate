package com.shopmate.payment.service;

import com.shopmate.payment.dto.request.PaymentRequest;
import com.shopmate.payment.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse pay(Long userId, PaymentRequest request);

    List<PaymentResponse> getMyPayments(Long userId);

    PaymentResponse getPayment(Long userId, Long paymentId);

    List<PaymentResponse> getPaymentsForOrder(Long userId, Long orderId);
}
