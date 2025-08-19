package com.harsh.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendOrderConfirmationEmail(String to, String orderNumber, double totalAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Order Confirmation - " + orderNumber);
            message.setText("Thank you for your order!\n\n" +
                    "Order Number: " + orderNumber + "\n" +
                    "Total Amount: $" + String.format("%.2f", totalAmount) + "\n\n" +
                    "We will notify you when your order is shipped.\n\n" +
                    "Best regards,\nYour E-Commerce Team");
            mailSender.send(message);
            System.out.println("✅ Order confirmation email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send order confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendPaymentSuccessEmail(String to, String orderNumber, double amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Payment Successful - " + orderNumber);
            message.setText("Your payment has been processed successfully!\n\n" +
                    "Order Number: " + orderNumber + "\n" +
                    "Amount Paid: $" + String.format("%.2f", amount) + "\n\n" +
                    "Your order is now being processed and will be shipped soon.\n\n" +
                    "Best regards,\nYour E-Commerce Team");
            mailSender.send(message);
            System.out.println("✅ Payment success email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send payment success email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendPaymentFailedEmail(String to, String orderNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Payment Failed - " + orderNumber);
            message.setText("We were unable to process your payment.\n\n" +
                    "Order Number: " + orderNumber + "\n\n" +
                    "Please try again or contact our customer support team if you need assistance.\n\n" +
                    "Best regards,\nYour E-Commerce Team");
            mailSender.send(message);
            System.out.println("✅ Payment failed email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send payment failed email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendShippingNotificationEmail(String to, String orderNumber, String trackingNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Order Shipped - " + orderNumber);
            message.setText("Great news! Your order has been shipped.\n\n" +
                    "Order Number: " + orderNumber + "\n" +
                    "Tracking Number: " + trackingNumber + "\n\n" +
                    "You can track your package using the tracking number above.\n" +
                    "Expected delivery: 3-5 business days.\n\n" +
                    "Best regards,\nYour E-Commerce Team");
            mailSender.send(message);
            System.out.println("✅ Shipping notification email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send shipping notification email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendDeliveryConfirmationEmail(String to, String orderNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Order Delivered - " + orderNumber);
            message.setText("Your order has been delivered successfully!\n\n" +
                    "Order Number: " + orderNumber + "\n\n" +
                    "We hope you're satisfied with your purchase. " +
                    "If you have any concerns, please don't hesitate to contact us.\n\n" +
                    "Thank you for shopping with us!\n\n" +
                    "Best regards,\nYour E-Commerce Team");
            mailSender.send(message);
            System.out.println("✅ Delivery confirmation email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send delivery confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendOrderCancellationEmail(String to, String orderNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Order Cancelled - " + orderNumber);
            message.setText("Your order has been cancelled successfully.\n\n" +
                    "Order Number: " + orderNumber + "\n\n" +
                    "If this was done in error, please contact our customer support team. " +
                    "Any charges for this order will be refunded within 3-5 business days.\n\n" +
                    "Best regards,\nYour E-Commerce Team");
            mailSender.send(message);
            System.out.println("✅ Order cancellation email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send order cancellation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to our E-Commerce Store!");
            message.setText("Welcome " + name + "!\n\n" +
                    "Thank you for registering with us. We're excited to have you as part of our community.\n\n" +
                    "Start shopping and enjoy exclusive deals and offers available only to our registered customers.\n\n" +
                    "If you have any questions, feel free to contact our support team.\n\n" +
                    "Happy shopping!\n\n" +
                    "Best regards,\nYour E-Commerce Team");
            mailSender.send(message);
            System.out.println("✅ Welcome email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendRefundConfirmationEmail(String to, String orderNumber, double refundAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Refund Processed - " + orderNumber);
            message.setText("Your refund has been processed successfully.\n\n" +
                    "Order Number: " + orderNumber + "\n" +
                    "Refund Amount: $" + String.format("%.2f", refundAmount) + "\n\n" +
                    "The refund will appear in your original payment method within 3-5 business days.\n\n" +
                    "Best regards,\nYour E-Commerce Team");
            mailSender.send(message);
            System.out.println("✅ Refund confirmation email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send refund confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}