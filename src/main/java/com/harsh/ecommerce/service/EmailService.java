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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Order Confirmation - " + orderNumber);
        message.setText("Thank you for your order!\n\n" +
                "Order Number: " + orderNumber + "\n" +
                "Total Amount: $" + totalAmount + "\n\n" +
                "We will notify you when your order is shipped.");
        mailSender.send(message);
    }

    @Async
    public void sendPaymentSuccessEmail(String to, String orderNumber, double amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Payment Successful - " + orderNumber);
        message.setText("Your payment has been processed successfully!\n\n" +
                "Order Number: " + orderNumber + "\n" +
                "Amount Paid: $" + amount + "\n\n" +
                "Your order is now being processed.");
        mailSender.send(message);
    }

    @Async
    public void sendPaymentFailedEmail(String to, String orderNumber) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Payment Failed - " + orderNumber);
        message.setText("We were unable to process your payment.\n\n" +
                "Order Number: " + orderNumber + "\n\n" +
                "Please try again or contact customer support.");
        mailSender.send(message);
    }

    @Async
    public void sendShippingNotificationEmail(String to, String orderNumber, String trackingNumber) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Order Shipped - " + orderNumber);
        message.setText("Great news! Your order has been shipped.\n\n" +
                "Order Number: " + orderNumber + "\n" +
                "Tracking Number: " + trackingNumber + "\n\n" +
                "You can track your package using the tracking number.");
        mailSender.send(message);
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to our E-Commerce Store!");
        message.setText("Welcome " + name + "!\n\n" +
                "Thank you for registering with us. We're excited to have you as part of our community.\n\n" +
                "Start shopping and enjoy exclusive deals!");
        mailSender.send(message);
    }
}