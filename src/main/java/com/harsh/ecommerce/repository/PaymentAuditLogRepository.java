package com.harsh.ecommerce.repository;

import com.harsh.ecommerce.entity.PaymentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, Long> {
    List<PaymentAuditLog> findByPaymentIdOrderByTimestampDesc(Long paymentId);
    List<PaymentAuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<PaymentAuditLog> findByAction(String action);
}