package com.harsh.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    private Long refundId;
    private String stripeRefundId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime processedAt;
}