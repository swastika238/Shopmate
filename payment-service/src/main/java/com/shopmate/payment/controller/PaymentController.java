package com.shopmate.payment.controller;

import com.shopmate.payment.dto.request.PaymentRequest;
import com.shopmate.payment.dto.response.PaymentResponse;
import com.shopmate.payment.entity.PaymentStatus;
import com.shopmate.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@RequestHeader("X-User-Id") Long userId,
                                               @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.pay(userId, request);
        HttpStatus status = response.status() == PaymentStatus.SUCCESS
                ? HttpStatus.OK
                : HttpStatus.PAYMENT_REQUIRED; // 402 when the payment failed
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping
    public List<PaymentResponse> myPayments(@RequestHeader("X-User-Id") Long userId) {
        return paymentService.getMyPayments(userId);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@RequestHeader("X-User-Id") Long userId, @PathVariable("id") Long id) {
        return paymentService.getPayment(userId, id);
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentResponse> forOrder(@RequestHeader("X-User-Id") Long userId,
                                          @PathVariable("orderId") Long orderId) {
        return paymentService.getPaymentsForOrder(userId, orderId);
    }
}
