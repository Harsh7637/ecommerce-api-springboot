package com.harsh.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewDto {
    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private String title; // Added missing title field
    private String comment;
    private int rating;
    private boolean approved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}