package com.harsh.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDto {
    private double averageRating;
    private long totalReviews;
    private long fiveStarReviews;
    private long fourStarReviews;
    private long threeStarReviews;
    private long twoStarReviews;
    private long oneStarReviews;
}
