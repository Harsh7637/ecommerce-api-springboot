package com.harsh.ecommerce.entity;

public enum OrderStatus {
    PENDING("Order placed, awaiting processing"),
    CONFIRMED("Payment confirmed, order is being prepared"),
    PROCESSING("Order is being processed"),
    SHIPPED("Order has been shipped"),
    DELIVERED("Order delivered successfully"),
    CANCELLED("Order cancelled"),
    REFUNDED("Order refunded");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
