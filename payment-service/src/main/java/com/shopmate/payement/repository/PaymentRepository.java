package com.shopmate.payement.repository;

import com.shopmate.payment.entity.Payment;
import com.shopmate.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByOrderIdAndUserIdOrderByCreatedAtDesc(Long orderId, Long userId);

    Optional<Payment> findFirstByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
