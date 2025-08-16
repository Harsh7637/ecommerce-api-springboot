package com.harsh.ecommerce.service;

import com.harsh.ecommerce.dto.*;
import com.harsh.ecommerce.entity.*;
import com.harsh.ecommerce.exception.OrderNotFoundException;
import com.harsh.ecommerce.exception.PaymentNotFoundException;
import com.harsh.ecommerce.exception.PaymentProcessingException;
import com.harsh.ecommerce.exception.UnauthorizedAccessException;
import com.harsh.ecommerce.repository.PaymentRepository;
import com.harsh.ecommerce.repository.RefundRepository;
import com.harsh.ecommerce.repository.PaymentAuditLogRepository;
import com.harsh.ecommerce.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentAuditLogRepository auditLogRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository; // Add this

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public PaymentService(PaymentRepository paymentRepository,
                          RefundRepository refundRepository,
                          PaymentAuditLogRepository auditLogRepository,
                          OrderService orderService,
                          OrderRepository orderRepository) { // Add this parameter
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.auditLogRepository = auditLogRepository;
        this.orderService = orderService;
        this.orderRepository = orderRepository; // Add this
    }

    // Updated method to include userId validation
    // Add this method to your PaymentService class
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request, Long userId) {
        try {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + request.getOrderId()));

            if (!order.getUser().getId().equals(userId)) {
                throw new UnauthorizedAccessException("Order does not belong to the authenticated user");
            }

            Optional<Payment> existingPayment = paymentRepository.findByOrder_Id(request.getOrderId());
            if (existingPayment.isPresent()) {
                throw new PaymentProcessingException("Payment already exists for this order");
            }

            Stripe.apiKey = stripeSecretKey;

            // FIXED: Create PaymentIntent with simpler configuration for testing
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount().multiply(new BigDecimal("100")).longValue())
                    .setCurrency(request.getCurrency())
                    .setReceiptEmail(request.getCustomerEmail())
                    .putMetadata("order_id", String.valueOf(request.getOrderId()))
                    .putMetadata("user_id", String.valueOf(userId))
                    // FIXED: Use manual confirmation for testing
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                    // FIXED: Disable automatic payment methods to avoid redirect issues
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(false)
                                    .build()
                    )
                    .addPaymentMethodType("card") // Only allow cards for testing
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setStatus(PaymentTransactionStatus.PENDING);
            payment.setPaymentMethod(PaymentMethodType.CARD);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            return PaymentIntentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .build();

        } catch (StripeException e) {
            throw new PaymentProcessingException("Failed to create payment intent: " + e.getMessage());
        }
    }

    // Keep the old method for backward compatibility, but make it call the new one
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) {
        throw new PaymentProcessingException("User ID is required for payment processing");
    }

    public PaymentDto confirmPayment(PaymentConfirmRequest request) {
        try {
            Stripe.apiKey = stripeSecretKey;

            // CRITICAL FIX: Actually retrieve and verify the PaymentIntent status
            PaymentIntent paymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());

            System.out.println("🔍 Stripe PaymentIntent Status: " + paymentIntent.getStatus());
            System.out.println("🔍 Amount Received: " + paymentIntent.getAmountReceived());

            Optional<Payment> optionalPayment = paymentRepository.findByStripePaymentIntentId(request.getPaymentIntentId());
            if (optionalPayment.isEmpty()) {
                throw new PaymentNotFoundException("Payment not found for intent: " + request.getPaymentIntentId());
            }

            Payment payment = optionalPayment.get();

            // CRITICAL FIX: Only mark as SUCCEEDED if Stripe actually shows it as succeeded
            PaymentTransactionStatus newStatus;
            switch (paymentIntent.getStatus()) {
                case "succeeded":
                    newStatus = PaymentTransactionStatus.SUCCEEDED;
                    break;
                case "requires_payment_method":
                case "canceled":
                    newStatus = PaymentTransactionStatus.FAILED;
                    break;
                case "processing":
                case "requires_action":
                case "requires_confirmation":
                    newStatus = PaymentTransactionStatus.PENDING;
                    break;
                default:
                    newStatus = PaymentTransactionStatus.PENDING;
            }

            payment.setStatus(newStatus);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            System.out.println("✅ Payment status updated to: " + newStatus);

            return convertToDto(payment);

        } catch (StripeException e) {
            throw new PaymentProcessingException("Failed to confirm payment: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getUserPaymentHistory(Long userId) {
        List<Payment> payments = paymentRepository.findByOrderUserIdOrderByCreatedAtDesc(userId);
        return payments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getUserPaymentHistoryPaginated(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByOrderUserIdOrderByCreatedAtDesc(userId, pageable);
        return payments.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
        return convertToDto(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByStripePaymentIntentId(String stripePaymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with stripe payment intent id: " + stripePaymentIntentId));
        return convertToDto(payment);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentDto> getPaymentByOrderId(Long orderId) {
        Optional<Payment> payment = paymentRepository.findByOrder_Id(orderId);
        return payment.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public PaymentAnalyticsDto getPaymentAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate);

        BigDecimal totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentTransactionStatus.SUCCEEDED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTransactions = payments.size();
        long successfulTransactions = payments.stream()
                .mapToLong(p -> p.getStatus() == PaymentTransactionStatus.SUCCEEDED ? 1 : 0)
                .sum();

        long failedTransactions = payments.stream()
                .mapToLong(p -> p.getStatus() == PaymentTransactionStatus.FAILED ? 1 : 0)
                .sum();

        double successRate = totalTransactions > 0 ? (double) successfulTransactions / totalTransactions * 100 : 0;

        return PaymentAnalyticsDto.builder()
                .totalRevenue(totalRevenue)
                .totalTransactions(totalTransactions)
                .successfulTransactions(successfulTransactions)
                .failedTransactions(failedTransactions)
                .successRate(successRate)
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getFailedPayments() {
        List<Payment> failedPayments = paymentRepository.findByStatus(PaymentTransactionStatus.FAILED);
        return failedPayments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getPaymentsByStatus(PaymentTransactionStatus status, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
        return payments.map(this::convertToDto);
    }

    public void updatePaymentStatus(String stripePaymentIntentId, PaymentTransactionStatus status) {
        Optional<Payment> optionalPayment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId);
        if (optionalPayment.isEmpty()) {
            throw new PaymentNotFoundException("Payment not found for intent: " + stripePaymentIntentId);
        }

        Payment payment = optionalPayment.get();
        PaymentTransactionStatus oldStatus = payment.getStatus();
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentAuditLog auditLog = new PaymentAuditLog();
        auditLog.setPayment(payment);
        auditLog.setAction("STATUS_UPDATE");
        auditLog.setOldValue(oldStatus.toString());
        auditLog.setNewValue(status.toString());
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    public void retryFailedPayment(String stripePaymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for intent: " + stripePaymentIntentId));

        if (payment.getStatus() != PaymentTransactionStatus.FAILED) {
            throw new PaymentProcessingException("Only failed payments can be retried");
        }

        payment.setStatus(PaymentTransactionStatus.PENDING);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentAuditLog auditLog = new PaymentAuditLog();
        auditLog.setPayment(payment);
        auditLog.setAction("RETRY_INITIATED");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    public RefundResponse processRefund(RefundRequest request) {
        try {
            Stripe.apiKey = stripeSecretKey;

            // Find payment in database
            Payment payment = paymentRepository.findByStripePaymentIntentId(request.getPaymentIntentId())
                    .orElseThrow(() -> new PaymentNotFoundException("Payment not found for intent: " + request.getPaymentIntentId()));

            System.out.println("🔍 Found payment in database:");
            System.out.println("   - Database ID: " + payment.getId());
            System.out.println("   - Payment Intent ID: " + payment.getStripePaymentIntentId());
            System.out.println("   - Database Status: " + payment.getStatus());
            System.out.println("   - Amount: " + payment.getAmount());

            // Check database status first
            if (payment.getStatus() != PaymentTransactionStatus.SUCCEEDED) {
                throw new PaymentProcessingException("Cannot refund payment with status: " + payment.getStatus() +
                        ". Only payments with SUCCEEDED status can be refunded.");
            }

            // Now check the actual Stripe PaymentIntent status
            PaymentIntent stripePaymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());
            System.out.println("🔍 Stripe PaymentIntent details:");
            System.out.println("   - Stripe Status: " + stripePaymentIntent.getStatus());
            System.out.println("   - Amount: " + stripePaymentIntent.getAmount());
            System.out.println("   - Amount Received: " + stripePaymentIntent.getAmountReceived());

            // Check if PaymentIntent was actually charged successfully
            if (!"succeeded".equals(stripePaymentIntent.getStatus())) {
                throw new PaymentProcessingException("Stripe PaymentIntent status is '" + stripePaymentIntent.getStatus() +
                        "', but must be 'succeeded' to process refund. Please check the payment status in Stripe dashboard.");
            }

            // Check if the PaymentIntent has been actually charged (amount received should be > 0)
            if (stripePaymentIntent.getAmountReceived() == null || stripePaymentIntent.getAmountReceived() == 0) {
                throw new PaymentProcessingException("PaymentIntent has not been charged yet. Amount received is 0. Cannot process refund without successful charge.");
            }

            // Validate refund amount
            BigDecimal maxRefundableAmount = new BigDecimal(stripePaymentIntent.getAmountReceived()).divide(new BigDecimal("100"));
            if (request.getAmount().compareTo(maxRefundableAmount) > 0) {
                throw new PaymentProcessingException("Refund amount (" + request.getAmount() +
                        ") cannot exceed the charged amount (" + maxRefundableAmount + ")");
            }

            // Process the refund
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(request.getPaymentIntentId())
                    .setAmount(request.getAmount().multiply(new BigDecimal("100")).longValue())
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .build();

            System.out.println("🔄 Processing refund with Stripe...");
            System.out.println("   - Amount to refund: " + request.getAmount());
            System.out.println("   - Amount in cents: " + request.getAmount().multiply(new BigDecimal("100")).longValue());

            Refund stripeRefund = Refund.create(params);

            System.out.println("✅ Stripe refund created:");
            System.out.println("   - Refund ID: " + stripeRefund.getId());
            System.out.println("   - Status: " + stripeRefund.getStatus());
            System.out.println("   - Amount: " + stripeRefund.getAmount());

            // Save refund to database
            com.harsh.ecommerce.entity.Refund refundEntity = new com.harsh.ecommerce.entity.Refund();
            refundEntity.setPayment(payment);
            refundEntity.setStripeRefundId(stripeRefund.getId());
            refundEntity.setAmount(request.getAmount());
            refundEntity.setReason(request.getReason());
            refundEntity.setStatus(RefundStatus.PENDING);
            refundEntity.setCreatedAt(LocalDateTime.now());

            com.harsh.ecommerce.entity.Refund savedRefund = refundRepository.save(refundEntity);

            // Create audit log
            PaymentAuditLog auditLog = new PaymentAuditLog();
            auditLog.setPayment(payment);
            auditLog.setAction("REFUND_INITIATED");
            auditLog.setNewValue(request.getAmount().toString());
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);

            return RefundResponse.builder()
                    .refundId(savedRefund.getId())
                    .stripeRefundId(savedRefund.getStripeRefundId())
                    .amount(savedRefund.getAmount())
                    .status(savedRefund.getStatus().toString())
                    .processedAt(savedRefund.getCreatedAt())
                    .build();

        } catch (StripeException e) {
            System.err.println("❌ Stripe error during refund: " + e.getMessage());
            System.err.println("   - Error Code: " + e.getCode());
            System.err.println("   - Request ID: " + e.getRequestId());
            throw new PaymentProcessingException("Failed to process refund: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ General error during refund: " + e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundHistory() {
        List<com.harsh.ecommerce.entity.Refund> refunds = refundRepository.findAll();
        return refunds.stream().map(this::convertRefundToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PaymentAuditLog> getPaymentAuditLog(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Async
    public void handlePaymentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (paymentIntent != null) {
            updatePaymentTransactionStatus(paymentIntent.getId(), PaymentTransactionStatus.SUCCEEDED);

            String orderId = paymentIntent.getMetadata().get("order_id");
            if (orderId != null) {
                Long id = Long.parseLong(orderId);
                orderService.updatePaymentStatus(id, PaymentStatus.COMPLETED);
                orderService.updateOrderStatus(id, OrderStatus.CONFIRMED);
            }
        }
    }

    @Async
    public void handlePaymentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (paymentIntent != null) {
            updatePaymentTransactionStatus(paymentIntent.getId(), PaymentTransactionStatus.FAILED);

            String orderId = paymentIntent.getMetadata().get("order_id");
            if (orderId != null) {
                Long id = Long.parseLong(orderId);
                orderService.updatePaymentStatus(id, PaymentStatus.FAILED);
                orderService.updateOrderStatus(id, OrderStatus.CANCELLED);
            }
        }
    }

    public void syncPaymentStatusWithStripe(String paymentIntentId) {
        try {
            Stripe.apiKey = stripeSecretKey;

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new PaymentNotFoundException("Payment not found for intent: " + paymentIntentId));

            PaymentIntent stripePaymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // CRITICAL FIX: Correct status mapping
            PaymentTransactionStatus actualStatus;
            switch (stripePaymentIntent.getStatus()) {
                case "succeeded":
                    actualStatus = PaymentTransactionStatus.SUCCEEDED;
                    break;
                case "requires_payment_method": // This means payment failed/was never completed
                case "canceled":
                    actualStatus = PaymentTransactionStatus.FAILED;
                    break;
                case "requires_confirmation":
                case "requires_action":
                case "processing":
                case "requires_capture":
                    actualStatus = PaymentTransactionStatus.PENDING;
                    break;
                default:
                    actualStatus = PaymentTransactionStatus.FAILED; // Default to failed for unknown statuses
            }

            System.out.println("🔄 Syncing payment status:");
            System.out.println("  - Database Status: " + payment.getStatus());
            System.out.println("  - Stripe Status: " + stripePaymentIntent.getStatus());
            System.out.println("  - New Database Status: " + actualStatus);

            if (payment.getStatus() != actualStatus) {
                PaymentTransactionStatus oldStatus = payment.getStatus();
                payment.setStatus(actualStatus);
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                PaymentAuditLog auditLog = new PaymentAuditLog();
                auditLog.setPayment(payment);
                auditLog.setAction("STATUS_SYNC");
                auditLog.setOldValue(oldStatus.toString());
                auditLog.setNewValue(actualStatus.toString());
                auditLog.setTimestamp(LocalDateTime.now());
                auditLogRepository.save(auditLog);

                System.out.println("✅ Payment status synced successfully");
            } else {
                System.out.println("ℹ️  Payment status already in sync");
            }

        } catch (StripeException e) {
            throw new PaymentProcessingException("Failed to sync payment status: " + e.getMessage());
        }
    }

    public void updatePaymentTransactionStatusByIntentId(String stripePaymentIntentId, PaymentTransactionStatus status) {
        Optional<Payment> optionalPayment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId);

        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setStatus(status);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            System.out.println("💾 Updated existing payment status to: " + status + " for intent: " + stripePaymentIntentId);

            if (status == PaymentTransactionStatus.SUCCEEDED && payment.getOrder() != null) {
                try {
                    orderService.updatePaymentStatus(payment.getOrder().getId(), PaymentStatus.COMPLETED);
                    orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.CONFIRMED);
                    System.out.println("📦 Updated order status to CONFIRMED");
                } catch (Exception e) {
                    System.err.println("⚠️  Failed to update order status: " + e.getMessage());
                }
            } else if (status == PaymentTransactionStatus.FAILED && payment.getOrder() != null) {
                try {
                    orderService.updatePaymentStatus(payment.getOrder().getId(), PaymentStatus.FAILED);
                    orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.CANCELLED);
                    System.out.println("📦 Updated order status to CANCELLED");
                } catch (Exception e) {
                    System.err.println("⚠️  Failed to update order status: " + e.getMessage());
                }
            }
        } else {
            // REMOVED: Mock payment creation - this was causing data inconsistencies
            System.err.println("⚠️  Payment not found for intent ID: " + stripePaymentIntentId);
            throw new PaymentNotFoundException("Payment not found for intent: " + stripePaymentIntentId);
        }
    }

    private void updatePaymentTransactionStatus(String stripePaymentIntentId, PaymentTransactionStatus transactionStatus) {
        Optional<Payment> optionalPayment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setStatus(transactionStatus);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }
    }

    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setStripePaymentId(payment.getStripePaymentId());
        dto.setStripePaymentIntentId(payment.getStripePaymentIntentId());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setFailureReason(payment.getFailureReason());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }

    private RefundResponse convertRefundToDto(com.harsh.ecommerce.entity.Refund refund) {
        return RefundResponse.builder()
                .refundId(refund.getId())
                .stripeRefundId(refund.getStripeRefundId())
                .amount(refund.getAmount())
                .status(refund.getStatus().toString())
                .processedAt(refund.getCreatedAt())
                .build();
    }

    public Payment createPaymentIntent(Order order, long amount, String currency, String customerEmail) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setReceiptEmail(customerEmail)
                .putMetadata("order_id", String.valueOf(order.getId()))
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStripePaymentIntentId(paymentIntent.getId());
        payment.setStatus(PaymentTransactionStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return payment;
    }
}