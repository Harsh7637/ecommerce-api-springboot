package com.harsh.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDto {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "City is required")
    private String shippingCity;

    @NotBlank(message = "State is required")
    private String shippingState;

    @NotBlank(message = "Postal code is required")
    private String shippingPostalCode;

    @NotBlank(message = "Country is required")
    private String shippingCountry;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String notes;
}
