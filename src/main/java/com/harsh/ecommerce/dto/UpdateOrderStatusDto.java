package com.harsh.ecommerce.dto;

import com.harsh.ecommerce.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusDto {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    private String notes;

    public UpdateOrderStatusDto(OrderStatus status) {
        this.status = status;
    }
}
