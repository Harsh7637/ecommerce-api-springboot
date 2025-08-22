package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.ProductReviewDto;
import com.harsh.ecommerce.service.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
@Tag(name = "👨‍💼 Admin - Reviews", description = "Review moderation (admin only)")
public class AdminReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @GetMapping
    @Operation(summary = "Get all reviews (Admin)", description = "Retrieves all product reviews, including pending and approved ones, with pagination. Admin only.")
    @ApiResponse(responseCode = "200", description = "All reviews retrieved successfully")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<List<ProductReviewDto>>> getAllReviews(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        List<ProductReviewDto> reviews = productReviewService.getAllReviews();

        // Simple pagination simulation (you might want to implement proper pagination in service)
        int start = page * size;
        int end = Math.min(start + size, reviews.size());
        List<ProductReviewDto> paginatedReviews = reviews.subList(
                Math.min(start, reviews.size()),
                Math.min(end, reviews.size())
        );

        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "All reviews retrieved successfully", paginatedReviews));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending reviews (Admin)", description = "Retrieves a list of all reviews that are awaiting moderation. Admin only.")
    @ApiResponse(responseCode = "200", description = "Pending reviews retrieved successfully")
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<List<ProductReviewDto>>> getPendingReviews() {
        List<ProductReviewDto> reviews = productReviewService.getPendingReviews();
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Pending reviews retrieved successfully", reviews));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all reviews for a product (Admin)", description = "Retrieves all reviews, both pending and approved, for a specific product. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All product reviews retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<List<ProductReviewDto>>> getAllProductReviews(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long productId) {
        List<ProductReviewDto> reviews = productReviewService.getAllReviewsByProductId(productId);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "All product reviews retrieved successfully", reviews));
    }

    @PutMapping("/{reviewId}/approve")
    @Operation(summary = "Approve a review (Admin)", description = "Approves a pending review, making it visible to the public. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review approved successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<ProductReviewDto>> approveReview(
            @Parameter(description = "Review ID", example = "1")
            @PathVariable Long reviewId) {
        ProductReviewDto review = productReviewService.approveReview(reviewId);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Review approved successfully", review));
    }

    @PutMapping("/{reviewId}/moderate")
    @Operation(summary = "Moderate a review (Admin)", description = "Updates a review's moderation status and adds notes. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review moderated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<ProductReviewDto>> moderateReview(
            @Parameter(description = "Review ID", example = "1")
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> moderationRequest) {

        Boolean approved = (Boolean) moderationRequest.get("approved");
        String moderatorNotes = (String) moderationRequest.get("moderatorNotes");

        if (approved == null) {
            approved = false; // Default to not approved
        }

        ProductReviewDto review = productReviewService.moderateReview(reviewId, approved, moderatorNotes);

        String message = approved ? "Review approved successfully" : "Review rejected successfully";
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, message, review));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review (Admin)", description = "Deletes a review from the system. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<String>> deleteReview(
            @Parameter(description = "Review ID", example = "1")
            @PathVariable Long reviewId) {
        productReviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Review deleted successfully", null));
    }
}