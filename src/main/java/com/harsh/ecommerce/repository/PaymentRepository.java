package com.harsh.ecommerce.repository;

import com.harsh.ecommerce.entity.Payment;
import com.harsh.ecommerce.entity.PaymentTransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    Optional<Payment> findByOrder_Id(Long orderId);
    List<Payment> findByOrderUserIdOrderByCreatedAtDesc(Long userId);
    Page<Payment> findByOrderUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Payment> findByStatus(PaymentTransactionStatus status);
    Page<Payment> findByStatus(PaymentTransactionStatus status, Pageable pageable);
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

}