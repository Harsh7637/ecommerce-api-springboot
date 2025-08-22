package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.PaymentAnalyticsDto;
import com.harsh.ecommerce.dto.PaymentDto;
import com.harsh.ecommerce.dto.UpdatePaymentTransactionStatusDto;
import com.harsh.ecommerce.entity.PaymentAuditLog;
import com.harsh.ecommerce.entity.PaymentTransactionStatus;
import com.harsh.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@CrossOrigin(origins = "*")
@Tag(name = "👨‍💼 Admin - Payments", description = "Payment administration (admin only)")
public class AdminPaymentController {

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get payment analytics", description = "Retrieves payment statistics within a given date range. Admin only.")
    @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<PaymentAnalyticsDto>> getPaymentAnalytics(
            @Parameter(description = "Start date for analytics (ISO 8601 format)", example = "2023-01-01T00:00:00")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date for analytics (ISO 8601 format)", example = "2023-01-31T23:59:59")
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();

            PaymentAnalyticsDto analytics = paymentService.getPaymentAnalytics(start, end);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payment analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to retrieve payment analytics: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{paymentIntentId}/status")
    @Operation(summary = "Update payment status", description = "Manually updates the status of a payment transaction. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment ID or status")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<String>> updatePaymentStatus(
            @Parameter(description = "Payment Intent ID", example = "pi_123...")
            @PathVariable String paymentIntentId,
            @RequestBody UpdatePaymentTransactionStatusDto request) {
        try {
            PaymentTransactionStatus status = PaymentTransactionStatus.valueOf(request.getStatus().toUpperCase());
            paymentService.updatePaymentStatus(paymentIntentId, status);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payment status updated successfully", "Status updated to " + status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to update payment status: " + e.getMessage(), null));
        }
    }

    @GetMapping("")
    @Operation(summary = "Get all payments", description = "Retrieves a paginated list of all payments, with optional filtering by status. Admin only.")
    @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<Page<PaymentDto>>> getAllPayments(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by payment status", example = "SUCCEEDED", schema = @Schema(implementation = PaymentTransactionStatus.class))
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

            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payments retrieved successfully", payments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to retrieve payments: " + e.getMessage(), null));
        }
    }

    @GetMapping("/audit-log")
    @Operation(summary = "Get payment audit log", description = "Retrieves a paginated list of all payment audit logs. Admin only.")
    @ApiResponse(responseCode = "200", description = "Audit log retrieved successfully")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<Page<PaymentAuditLog>>> getPaymentAuditLog(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PaymentAuditLog> auditLogs = paymentService.getPaymentAuditLog(pageable);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Audit log retrieved successfully", auditLogs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to retrieve audit log: " + e.getMessage(), null));
        }
    }
}