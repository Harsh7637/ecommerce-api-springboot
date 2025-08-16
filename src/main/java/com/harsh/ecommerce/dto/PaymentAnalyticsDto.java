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
public class PaymentAnalyticsDto {
    private BigDecimal totalRevenue;
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private Double successRate;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}