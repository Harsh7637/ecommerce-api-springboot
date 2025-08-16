package com.harsh.ecommerce.entity;

public enum PaymentStatus {
    PENDING("Payment pending"),
    PROCESSING("Payment being processed"),
    COMPLETED("Payment completed successfully"),
    FAILED("Payment failed"),
    CANCELLED("Payment cancelled"),
    REFUNDED("Payment refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
