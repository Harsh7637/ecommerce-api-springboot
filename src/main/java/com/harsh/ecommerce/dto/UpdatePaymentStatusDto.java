package com.harsh.ecommerce.dto;

import com.harsh.ecommerce.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePaymentStatusDto {
    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    private String notes;

    public UpdatePaymentStatusDto(PaymentStatus status) {
        this.status = status;
    }
}