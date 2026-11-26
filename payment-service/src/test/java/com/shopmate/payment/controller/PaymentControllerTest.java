package com.shopmate.payment.controller;

import com.shopmate.payment.dto.response.PaymentResponse;
import com.shopmate.payment.entity.PaymentMethod;
import com.shopmate.payment.entity.PaymentStatus;
import com.shopmate.payment.exception.GlobalExceptionHandler;
import com.shopmate.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    private PaymentService paymentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PaymentController(paymentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private PaymentResponse response(PaymentStatus status) {
        return new PaymentResponse(1L, 5L, 1L, new BigDecimal("1000.00"), PaymentMethod.KHALTI,
                status, "TXN-1", null, LocalDateTime.now());
    }

    @Test
    void pay_success_returns200() throws Exception {
        when(paymentService.pay(eq(1L), any())).thenReturn(response(PaymentStatus.SUCCESS));

        mockMvc.perform(post("/api/payments")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":5,\"method\":\"KHALTI\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void pay_failed_returns402() throws Exception {
        when(paymentService.pay(eq(1L), any())).thenReturn(response(PaymentStatus.FAILED));

        mockMvc.perform(post("/api/payments")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":5,\"method\":\"CARD\",\"simulateFailure\":true}"))
                .andExpect(status().isPaymentRequired());
    }

    @Test
    void pay_missingOrderId_returns400() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\":\"CARD\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pay_invalidMethod_returns400() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":5,\"method\":\"BITCOIN\"}"))
                .andExpect(status().isBadRequest());
    }
}
