package com.shopmate.payement.service;

import com.shopmate.payement.client.OrderClient;
import com.shopmate.payement.client.OrderResponse;
import com.shopmate.payement.client.OrderStatusUpdateRequest;
import com.shopmate.payement.dto.request.PaymentRequest;
import com.shopmate.payment.dto.response.PaymentResponse;
import com.shopmate.payement.entity.Payment;
import com.shopmate.payement.entity.PaymentStatus;
import com.shopmate.payement.exception.OrderNotFoundException;
import com.shopmate.payement.exception.OrderUpdateFailedException;
import com.shopmate.payement.exception.PaymentNotFoundException;
import com.shopmate.payement.repository.PaymentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * FAKE payment flow (Stage A). Replace simulateGateway logic with Khalti/eSewa in Stage B.
 * Not wrapped in one big @Transactional on purpose: remote calls to order-service must not
 * hold a DB transaction open, and each saved payment state should be committed.
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String ORDER_PENDING = "PENDING";
    private static final String ORDER_PAID = "PAID";

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    @Override
    public PaymentResponse pay(Long userId, PaymentRequest request) {
        // 1. Load the order from order-service (the amount always comes from the order)
        OrderResponse order = orderClient.getOrder(request.orderId());
        if (!order.userId().equals(userId)) {
            throw new OrderNotFoundException(request.orderId()); // don't reveal other users' orders
        }

        // 2. Idempotency: an order can only be paid once
        Optional<Payment> existing =
                paymentRepository.findFirstByOrderIdAndStatus(order.id(), PaymentStatus.SUCCESS);
        if (existing.isPresent()) {
            Payment paid = existing.get();
            if (ORDER_PENDING.equals(order.status())) {
                // earlier payment succeeded but the order update failed: sync it now, do not charge again
                markOrderPaid(paid);
            }
            return PaymentResponse.from(paid);
        }

        if (!ORDER_PENDING.equals(order.status())) {
            throw new IllegalStateException("Order is not payable. Current status: " + order.status());
        }

        // 3. Record the attempt
        Payment payment = new Payment();
        payment.setOrderId(order.id());
        payment.setUserId(userId);
        payment.setAmount(order.totalAmount());
        payment.setMethod(request.method());
        payment.setStatus(PaymentStatus.INITIATED);
        payment = paymentRepository.save(payment);

        // 4. Fake gateway
        if (Boolean.TRUE.equals(request.simulateFailure())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Simulated payment failure");
            return PaymentResponse.from(paymentRepository.save(payment));
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN-" + UUID.randomUUID());
        payment = paymentRepository.save(payment);

        // 5. Tell order-service the order is paid
        markOrderPaid(payment);
        return PaymentResponse.from(payment);
    }

    @Override
    public List<PaymentResponse> getMyPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(PaymentResponse::from).toList();
    }

    @Override
    public PaymentResponse getPayment(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        if (!payment.getUserId().equals(userId)) {
            throw new PaymentNotFoundException(paymentId);
        }
        return PaymentResponse.from(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsForOrder(Long userId, Long orderId) {
        return paymentRepository.findByOrderIdAndUserIdOrderByCreatedAtDesc(orderId, userId)
                .stream().map(PaymentResponse::from).toList();
    }

    private void markOrderPaid(Payment payment) {
        try {
            orderClient.updateOrderStatus(payment.getOrderId(), new OrderStatusUpdateRequest(ORDER_PAID));
        } catch (FeignException e) {
            throw new OrderUpdateFailedException(
                    "Payment succeeded (" + payment.getTransactionId()
                            + ") but the order could not be updated. Retry the payment request to sync it; you will not be charged twice.",
                    e);
        }
    }
}

