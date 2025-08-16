package com.harsh.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdatePaymentTransactionStatusDto {

    @NotNull
    @NotBlank
    private String status;

    private String reason; // Optional reason for status change

    // Default constructor
    public UpdatePaymentTransactionStatusDto() {}

    // Constructor with parameters
    public UpdatePaymentTransactionStatusDto(String status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "UpdatePaymentTransactionStatusDto{" +
                "status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}