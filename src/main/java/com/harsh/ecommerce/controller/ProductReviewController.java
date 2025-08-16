package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.ApiResponse;
import com.harsh.ecommerce.dto.CreateReviewDto;
import com.harsh.ecommerce.dto.ProductReviewDto;
import com.harsh.ecommerce.dto.ReviewStatsDto;
import com.harsh.ecommerce.entity.User;
import com.harsh.ecommerce.service.ProductReviewService;
import com.harsh.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductReviewDto>>> getProductReviews(@PathVariable Long productId) {
        List<ProductReviewDto> reviews = productReviewService.getApprovedReviewsByProductId(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reviews retrieved successfully", reviews));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ReviewStatsDto>> getReviewStats(@PathVariable Long productId) {
        ReviewStatsDto stats = productReviewService.getReviewStats(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review statistics retrieved successfully", stats));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductReviewDto>> createReview(
            @PathVariable Long productId,
            @RequestBody CreateReviewDto createReviewDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // Get email from authentication
        User user = userService.findByEmail(userEmail); // Get user by email
        Long userId = user.getId(); // Extract userId

        ProductReviewDto review = productReviewService.createReview(productId, userId, createReviewDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review created successfully", review));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductReviewDto>> updateReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @RequestBody CreateReviewDto updateReviewDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // Get email from authentication
        User user = userService.findByEmail(userEmail); // Get user by email
        Long userId = user.getId(); // Extract userId

        ProductReviewDto review = productReviewService.updateReview(reviewId, userId, updateReviewDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review updated successfully", review));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // Get email from authentication
        User user = userService.findByEmail(userEmail); // Get user by email
        Long userId = user.getId(); // Extract userId

        productReviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review deleted successfully", null));
    }
}