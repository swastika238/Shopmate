package com.shopmate.payment.service;

import com.shopmate.common.dto.NotificationRequest;
import com.shopmate.payment.client.NotificationClient;
import com.shopmate.payment.client.OrderClient;
import com.shopmate.payment.client.OrderResponse;
import com.shopmate.payment.client.OrderStatusUpdateRequest;
import com.shopmate.payment.dto.request.PaymentRequest;
import com.shopmate.payment.dto.response.PaymentResponse;
import com.shopmate.payment.entity.Payment;
import com.shopmate.payment.entity.PaymentStatus;
import com.shopmate.payment.exception.OrderNotFoundException;
import com.shopmate.payment.exception.OrderUpdateFailedException;
import com.shopmate.payment.exception.PaymentNotFoundException;
import com.shopmate.payment.repository.PaymentRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String ORDER_PENDING = "PENDING";
    private static final String ORDER_PAID = "PAID";

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final NotificationClient notificationClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderClient orderClient) {
        this(paymentRepository, orderClient, null);
    }

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderClient orderClient,
                              NotificationClient notificationClient) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.notificationClient = notificationClient;
    }

    @Override
    public PaymentResponse pay(Long userId, PaymentRequest request) {
        OrderResponse order = orderClient.getOrder(request.orderId());
        if (!order.userId().equals(userId)) {
            throw new OrderNotFoundException(request.orderId());
        }

        Optional<Payment> existing =
                paymentRepository.findFirstByOrderIdAndStatus(order.id(), PaymentStatus.SUCCESS);
        if (existing.isPresent()) {
            Payment paid = existing.get();
            if (ORDER_PENDING.equals(order.status())) {
                markOrderPaid(paid);
            }
            return PaymentResponse.from(paid);
        }

        if (!ORDER_PENDING.equals(order.status())) {
            throw new IllegalStateException("Order is not payable. Current status: " + order.status());
        }

        Payment payment = new Payment();
        payment.setOrderId(order.id());
        payment.setUserId(userId);
        payment.setAmount(order.totalAmount());
        payment.setMethod(request.method());
        payment.setStatus(PaymentStatus.INITIATED);
        payment = paymentRepository.save(payment);

        if (Boolean.TRUE.equals(request.simulateFailure())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Simulated payment failure");
            Payment failed = paymentRepository.save(payment);
            notifyPayment(failed, "PAYMENT_FAILED",
                    "Payment Failed for Order #" + failed.getOrderId(),
                    "Your payment attempt via " + failed.getMethod() + " failed: " + failed.getFailureReason());
            return PaymentResponse.from(failed);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN-" + UUID.randomUUID());
        payment = paymentRepository.save(payment);

        markOrderPaid(payment);
        notifyPayment(payment, "PAYMENT_SUCCESS",
                "Payment Received for Order #" + payment.getOrderId(),
                "Payment of $" + payment.getAmount() + " via " + payment.getMethod()
                        + " succeeded. Transaction ID: " + payment.getTransactionId());
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

    private void notifyPayment(Payment payment, String type, String subject, String message) {
        if (notificationClient == null) {
            return;
        }
        try {
            notificationClient.send(new NotificationRequest(
                    payment.getUserId(), payment.getOrderId(), type, "IN_APP", null, subject, message));
        } catch (Exception ex) {
            log.warn("Failed to send {} notification for order {}: {}", type, payment.getOrderId(), ex.getMessage());
        }
    }
}
