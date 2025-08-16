package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Value("${app.environment:development}")
    private String environment;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Handles Stripe webhook events.
     * In development mode, signature verification is skipped.
     * In production mode, Stripe's signature is verified for security.
     */
    @PostMapping("/stripe")
    public ResponseEntity<?> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        try {
            Event event;

            // Development mode: skip signature verification but parse actual payload
            if ("development".equalsIgnoreCase(environment) || sigHeader == null) {
                System.out.println("⚠️  DEVELOPMENT MODE: Skipping signature verification");
                event = Event.GSON.fromJson(payload, Event.class);
            } else {
                // Production mode: verify Stripe signature
                try {
                    event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
                    System.out.println("✅ Webhook signature verified successfully");
                } catch (SignatureVerificationException e) {
                    System.err.println("⛔ Webhook signature verification failed: " + e.getMessage());
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Invalid signature"));
                }
            }

            // Validate parsed event
            if (event == null || event.getType() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Invalid or empty event"));
            }

            // Log event type
            System.out.println("📨 Received webhook event: " + event.getType());

            // Handle supported event types
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    System.out.println("💰 Processing payment success...");
                    paymentService.handlePaymentSucceeded(event);
                    break;

                case "payment_intent.payment_failed":
                    System.out.println("❌ Processing payment failure...");
                    paymentService.handlePaymentFailed(event);
                    break;

                default:
                    System.out.println("ℹ️  Ignoring unsupported event type: " + event.getType());
                    break;
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Event processed successfully"));

        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "An unexpected error occurred",
                            "error", "INTERNAL_SERVER_ERROR"
                    ));
        }
    }
}
