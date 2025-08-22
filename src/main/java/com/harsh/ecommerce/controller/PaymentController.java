package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.Security.JwtUtil;
import com.harsh.ecommerce.dto.*;
import com.harsh.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@Tag(name = "👤 User - Payments", description = "Payment processing (requires authentication)")
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    public PaymentController(PaymentService paymentService, JwtUtil jwtUtil) {
        this.paymentService = paymentService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create-payment-intent")
    @Operation(summary = "Create a new payment intent", description = "Initiates a new payment transaction and returns a client secret for the frontend.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment intent created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentIntentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Failed to create payment intent",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<PaymentIntentResponse>> createPaymentIntent(@Valid @RequestBody PaymentIntentRequest request, HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);

            PaymentIntentResponse response = paymentService.createPaymentIntent(request, userId);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payment intent created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to create payment intent: " + e.getMessage(), null));
        }
    }

    @PostMapping("/confirm-payment")
    @Operation(summary = "Confirm a payment", description = "Confirms a payment after a successful transaction from the client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment confirmed successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "400", description = "Failed to confirm payment",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<PaymentDto>> confirmPayment(@Valid @RequestBody PaymentConfirmRequest request, HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);

            PaymentDto payment = paymentService.confirmPayment(request);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payment confirmed successfully", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to confirm payment: " + e.getMessage(), null));
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get user's payment history", description = "Retrieves a list of all payments for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "Failed to retrieve payment history",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<List<PaymentDto>>> getPaymentHistory(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            List<PaymentDto> payments = paymentService.getUserPaymentHistory(userId);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payment history retrieved successfully", payments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to retrieve payment history: " + e.getMessage(), null));
        }
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID", description = "Retrieves payment details associated with a specific order.")
    @Parameter(description = "Order ID", example = "1", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<PaymentDto>> getPaymentByOrder(@PathVariable Long orderId, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            return paymentService.getPaymentByOrderId(orderId)
                    .map(payment -> ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payment retrieved successfully", payment)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to retrieve payment: " + e.getMessage(), null));
        }
    }

    @PostMapping("/refund")
    @Operation(summary = "Process a refund", description = "Initiates a refund for a payment (Admin only).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund processed successfully",
                    content = @Content(schema = @Schema(implementation = RefundResponse.class))),
            @ApiResponse(responseCode = "400", description = "Failed to process refund",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<RefundResponse>> processRefund(@Valid @RequestBody RefundRequest request) {
        try {
            RefundResponse refundResponse = paymentService.processRefund(request);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Refund processed successfully", refundResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to process refund: " + e.getMessage(), null));
        }
    }

    @GetMapping("/refunds")
    @Operation(summary = "Get refund history", description = "Retrieves a list of all processed refunds (Admin only).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "Failed to retrieve refund history",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<List<RefundResponse>>> getRefundHistory() {
        try {
            List<RefundResponse> refunds = paymentService.getRefundHistory();
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Refund history retrieved successfully", refunds));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to retrieve refund history: " + e.getMessage(), null));
        }
    }

    @PostMapping("/retry/{paymentIntentId}")
    @Operation(summary = "Retry a failed payment", description = "Attempts to retry a previously failed payment.")
    @Parameter(description = "ID of the payment intent", example = "pi_1Gk4bS...", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment retry initiated successfully",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Failed to retry payment",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<String>> retryFailedPayment(@PathVariable String paymentIntentId, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            paymentService.retryFailedPayment(paymentIntentId);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payment retry initiated successfully", "Payment retry initiated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to retry payment: " + e.getMessage(), null));
        }
    }

    @PostMapping("/sync-status/{paymentIntentId}")
    @Operation(summary = "Sync payment status with Stripe", description = "Manually syncs the status of a payment intent with the Stripe platform (Admin only).")
    @Parameter(description = "ID of the payment intent", example = "pi_1Gk4bS...", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment status synced successfully",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Failed to sync payment status",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<String>> syncPaymentStatus(@PathVariable String paymentIntentId) {
        try {
            paymentService.syncPaymentStatusWithStripe(paymentIntentId);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payment status synced successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to sync payment status: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{paymentIntentId}")
    @Operation(summary = "Get payment details by payment intent ID", description = "Retrieves payment details using the Stripe payment intent ID.")
    @Parameter(description = "ID of the payment intent", example = "pi_1Gk4bS...", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<PaymentDto>> getPaymentDetails(@PathVariable String paymentIntentId, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            PaymentDto payment = paymentService.getPaymentByStripePaymentIntentId(paymentIntentId);
            return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Payment details retrieved successfully", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.harsh.ecommerce.dto.ApiResponse<>(false, "Failed to retrieve payment details: " + e.getMessage(), null));
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