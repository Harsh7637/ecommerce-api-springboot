package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.Security.JwtUtil;
import com.harsh.ecommerce.dto.*;
import com.harsh.ecommerce.entity.PaymentAuditLog;
import com.harsh.ecommerce.entity.PaymentTransactionStatus;
import com.harsh.ecommerce.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    public PaymentController(PaymentService paymentService, JwtUtil jwtUtil) {
        this.paymentService = paymentService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> createPaymentIntent(@Valid @RequestBody PaymentIntentRequest request, HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);

            // Pass userId to the service method - THIS IS THE KEY FIX
            PaymentIntentResponse response = paymentService.createPaymentIntent(request, userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment intent created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to create payment intent: " + e.getMessage(), null));
        }
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<ApiResponse<PaymentDto>> confirmPayment(@Valid @RequestBody PaymentConfirmRequest request, HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);

            PaymentDto payment = paymentService.confirmPayment(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment confirmed successfully", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to confirm payment: " + e.getMessage(), null));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPaymentHistory(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            List<PaymentDto> payments = paymentService.getUserPaymentHistory(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment history retrieved successfully", payments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to retrieve payment history: " + e.getMessage(), null));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByOrder(@PathVariable Long orderId, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            return paymentService.getPaymentByOrderId(orderId)
                    .map(payment -> ResponseEntity.ok(new ApiResponse<>(true, "Payment retrieved successfully", payment)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to retrieve payment: " + e.getMessage(), null));
        }
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(@Valid @RequestBody RefundRequest request) {
        try {
            RefundResponse refundResponse = paymentService.processRefund(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Refund processed successfully", refundResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to process refund: " + e.getMessage(), null));
        }
    }

    @GetMapping("/refunds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundHistory() {
        try {
            List<RefundResponse> refunds = paymentService.getRefundHistory();
            return ResponseEntity.ok(new ApiResponse<>(true, "Refund history retrieved successfully", refunds));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to retrieve refund history: " + e.getMessage(), null));
        }
    }

    @PostMapping("/retry/{paymentIntentId}")
    public ResponseEntity<ApiResponse<String>> retryFailedPayment(@PathVariable String paymentIntentId, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            paymentService.retryFailedPayment(paymentIntentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment retry initiated successfully", "Payment retry initiated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to retry payment: " + e.getMessage(), null));
        }
    }
    // ADD THIS METHOD TO YOUR PaymentController.java class

    @PostMapping("/sync-status/{paymentIntentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> syncPaymentStatus(@PathVariable String paymentIntentId) {
        try {
            paymentService.syncPaymentStatusWithStripe(paymentIntentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment status synced successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to sync payment status: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{paymentIntentId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentDetails(@PathVariable String paymentIntentId, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            PaymentDto payment = paymentService.getPaymentByStripePaymentIntentId(paymentIntentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment details retrieved successfully", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to retrieve payment details: " + e.getMessage(), null));
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}