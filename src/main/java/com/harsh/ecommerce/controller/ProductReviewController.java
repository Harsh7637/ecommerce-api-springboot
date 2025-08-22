package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.CreateReviewDto;
import com.harsh.ecommerce.dto.ProductReviewDto;
import com.harsh.ecommerce.dto.ReviewStatsDto;
import com.harsh.ecommerce.entity.User;
import com.harsh.ecommerce.service.ProductReviewService;
import com.harsh.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@Tag(name = "🌐 Public - Reviews", description = "Public product reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @Autowired
    private UserService userService;

    @GetMapping("/{productId}/reviews")
    @Operation(summary = "Get reviews for a product", description = "Retrieves all approved reviews for a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductReviewDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<List<ProductReviewDto>>> getProductReviews(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable Long productId) {
        List<ProductReviewDto> reviews = productReviewService.getApprovedReviewsByProductId(productId);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Reviews retrieved successfully", reviews));
    }

    @GetMapping("/{productId}/reviews/stats")
    @Operation(summary = "Get review statistics for a product", description = "Retrieves rating distribution and average rating for a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ReviewStatsDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<ReviewStatsDto>> getReviewStats(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable Long productId) {
        ReviewStatsDto stats = productReviewService.getReviewStats(productId);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Review statistics retrieved successfully", stats));
    }

    @PostMapping("/{productId}/reviews")
    @Operation(summary = "Create a new review", description = "Creates a new review for a product. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review created successfully",
                    content = @Content(schema = @Schema(implementation = ProductReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid review data or user has already reviewed"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<ProductReviewDto>> createReview(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable Long productId,
            @RequestBody CreateReviewDto createReviewDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // Get email from authentication
        User user = userService.findByEmail(userEmail); // Get user by email
        Long userId = user.getId(); // Extract userId

        ProductReviewDto review = productReviewService.createReview(productId, userId, createReviewDto);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Review created successfully", review));
    }

    @PutMapping("/{productId}/reviews/{reviewId}")
    @Operation(summary = "Update an existing review", description = "Updates a review owned by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid review data"),
            @ApiResponse(responseCode = "403", description = "Forbidden: user does not own this review"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<ProductReviewDto>> updateReview(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable Long productId,
            @Parameter(description = "Review ID", example = "1", required = true)
            @PathVariable Long reviewId,
            @RequestBody CreateReviewDto updateReviewDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // Get email from authentication
        User user = userService.findByEmail(userEmail); // Get user by email
        Long userId = user.getId(); // Extract userId

        ProductReviewDto review = productReviewService.updateReview(reviewId, userId, updateReviewDto);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Review updated successfully", review));
    }

    @DeleteMapping("/{productId}/reviews/{reviewId}")
    @Operation(summary = "Delete a review", description = "Deletes a review owned by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: user does not own this review"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<String>> deleteReview(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable Long productId,
            @Parameter(description = "Review ID", example = "1", required = true)
            @PathVariable Long reviewId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // Get email from authentication
        User user = userService.findByEmail(userEmail); // Get user by email
        Long userId = user.getId(); // Extract userId

        productReviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Review deleted successfully", null));
    }
}