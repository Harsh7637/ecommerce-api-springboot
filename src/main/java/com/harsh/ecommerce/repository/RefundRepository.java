package com.harsh.ecommerce.repository;

import com.harsh.ecommerce.entity.Refund;
import com.harsh.ecommerce.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByStripeRefundId(String stripeRefundId);
    List<Refund> findByPaymentId(Long paymentId);
    List<Refund> findByStatus(RefundStatus status);
    List<Refund> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}