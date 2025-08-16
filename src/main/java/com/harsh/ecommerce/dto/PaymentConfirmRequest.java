package com.harsh.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmRequest {
    @NotBlank(message = "Payment Intent ID is required")
    private String paymentIntentId;

    private String paymentMethodId;
}