package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.ApiResponse;
import com.harsh.ecommerce.dto.PaymentAnalyticsDto;
import com.harsh.ecommerce.dto.PaymentDto;
import com.harsh.ecommerce.dto.UpdatePaymentTransactionStatusDto; // Added missing import
import com.harsh.ecommerce.entity.PaymentAuditLog;
import com.harsh.ecommerce.entity.PaymentTransactionStatus;
import com.harsh.ecommerce.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<PaymentAnalyticsDto>> getPaymentAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();

            PaymentAnalyticsDto analytics = paymentService.getPaymentAnalytics(start, end);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to retrieve payment analytics: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{paymentIntentId}/status")
    public ResponseEntity<ApiResponse<String>> updatePaymentStatus(
            @PathVariable String paymentIntentId,
            @RequestBody UpdatePaymentTransactionStatusDto request) {
        try {
            PaymentTransactionStatus status = PaymentTransactionStatus.valueOf(request.getStatus().toUpperCase());
            paymentService.updatePaymentStatus(paymentIntentId, status);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment status updated successfully", "Status updated to " + status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to update payment status: " + e.getMessage(), null));
        }
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PaymentDto> payments;

            if (status != null && !status.isEmpty()) {
                PaymentTransactionStatus paymentStatus = PaymentTransactionStatus.valueOf(status.toUpperCase());
                payments = paymentService.getPaymentsByStatus(paymentStatus, pageable);
            } else {
                payments = paymentService.getAllPayments(pageable);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Payments retrieved successfully", payments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to retrieve payments: " + e.getMessage(), null));
        }
    }

    @GetMapping("/audit-log")
    public ResponseEntity<ApiResponse<Page<PaymentAuditLog>>> getPaymentAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PaymentAuditLog> auditLogs = paymentService.getPaymentAuditLog(pageable);
            return ResponseEntity.ok(new ApiResponse<>(true, "Audit log retrieved successfully", auditLogs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to retrieve audit log: " + e.getMessage(), null));
        }
    }
}