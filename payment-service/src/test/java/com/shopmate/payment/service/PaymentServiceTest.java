package com.shopmate.payment.service;

import com.shopmate.payment.client.OrderClient;
import com.shopmate.payment.client.OrderResponse;
import com.shopmate.payment.client.OrderStatusUpdateRequest;
import com.shopmate.payment.dto.request.PaymentRequest;
import com.shopmate.payment.dto.response.PaymentResponse;
import com.shopmate.payment.entity.Payment;
import com.shopmate.payment.entity.PaymentMethod;
import com.shopmate.payment.entity.PaymentStatus;
import com.shopmate.payment.exception.OrderNotFoundException;
import com.shopmate.payment.exception.OrderUpdateFailedException;
import com.shopmate.payment.repository.PaymentRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private OrderClient orderClient;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        orderClient = mock(OrderClient.class);
        paymentService = new PaymentServiceImpl(paymentRepository, orderClient);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.findFirstByOrderIdAndStatus(5L, PaymentStatus.SUCCESS))
                .thenReturn(Optional.empty());
    }

    private OrderResponse order(Long userId, String status) {
        return new OrderResponse(5L, userId, status, new BigDecimal("1000.00"));
    }

    @Test
    void pay_success_marksOrderPaid() {
        when(orderClient.getOrder(5L)).thenReturn(order(1L, "PENDING"));

        PaymentResponse response = paymentService.pay(1L,
                new PaymentRequest(5L, PaymentMethod.KHALTI, false));

        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals(new BigDecimal("1000.00"), response.amount());
        assertTrue(response.transactionId().startsWith("TXN-"));

        ArgumentCaptor<OrderStatusUpdateRequest> captor =
                ArgumentCaptor.forClass(OrderStatusUpdateRequest.class);
        verify(orderClient).updateOrderStatus(eq(5L), captor.capture());
        assertEquals("PAID", captor.getValue().status());
    }

    @Test
    void pay_simulatedFailure_doesNotTouchOrder() {
        when(orderClient.getOrder(5L)).thenReturn(order(1L, "PENDING"));

        PaymentResponse response = paymentService.pay(1L,
                new PaymentRequest(5L, PaymentMethod.CARD, true));

        assertEquals(PaymentStatus.FAILED, response.status());
        verify(orderClient, never()).updateOrderStatus(any(), any());
    }

    @Test
    void pay_orderOfAnotherUser_throwsNotFound() {
        when(orderClient.getOrder(5L)).thenReturn(order(2L, "PENDING"));

        assertThrows(OrderNotFoundException.class, () ->
                paymentService.pay(1L, new PaymentRequest(5L, PaymentMethod.CARD, false)));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void pay_orderNotPending_throwsIllegalState() {
        when(orderClient.getOrder(5L)).thenReturn(order(1L, "CANCELLED"));

        assertThrows(IllegalStateException.class, () ->
                paymentService.pay(1L, new PaymentRequest(5L, PaymentMethod.CARD, false)));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void pay_alreadyPaidPaymentExists_doesNotChargeAgainButSyncsOrder() {
        Payment existing = new Payment();
        existing.setOrderId(5L);
        existing.setUserId(1L);
        existing.setAmount(new BigDecimal("1000.00"));
        existing.setMethod(PaymentMethod.CARD);
        existing.setStatus(PaymentStatus.SUCCESS);
        existing.setTransactionId("TXN-OLD");

        when(orderClient.getOrder(5L)).thenReturn(order(1L, "PENDING"));
        when(paymentRepository.findFirstByOrderIdAndStatus(5L, PaymentStatus.SUCCESS))
                .thenReturn(Optional.of(existing));

        PaymentResponse response = paymentService.pay(1L,
                new PaymentRequest(5L, PaymentMethod.CARD, false));

        assertEquals("TXN-OLD", response.transactionId());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(orderClient, times(1)).updateOrderStatus(eq(5L), any(OrderStatusUpdateRequest.class));
    }

    @Test
    void pay_orderUpdateFails_throwsOrderUpdateFailed() {
        when(orderClient.getOrder(5L)).thenReturn(order(1L, "PENDING"));
        when(orderClient.updateOrderStatus(eq(5L), any(OrderStatusUpdateRequest.class)))
                .thenThrow(mock(FeignException.class));

        assertThrows(OrderUpdateFailedException.class, () ->
                paymentService.pay(1L, new PaymentRequest(5L, PaymentMethod.CARD, false)));
    }
}
