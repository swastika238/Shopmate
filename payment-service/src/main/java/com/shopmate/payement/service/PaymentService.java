package com.shopmate.payement.service;

import com.shopmate.payement.dto.request.PaymentRequest;
import com.shopmate.payment.dto.request.PaymentRequest;
import com.shopmate.payment.dto.response.PaymentResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface PaymentService {
    PaymentResponse pay(Long userId, @Valid PaymentRequest request);

    List<PaymentResponse> getMyPayments(Long userId);

    PaymentResponse getPayment(Long userId, Long paymentId);

    List<PaymentResponse> getPaymentsForOrder(Long userId, Long orderId);
}
