package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.ApiResponse;
import com.harsh.ecommerce.dto.ProductReviewDto;
import com.harsh.ecommerce.service.ProductReviewService;
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
public class AdminReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    // GET /api/admin/reviews?page=0&size=10 - Get all reviews with pagination
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductReviewDto>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<ProductReviewDto> reviews = productReviewService.getAllReviews();

        // Simple pagination simulation (you might want to implement proper pagination in service)
        int start = page * size;
        int end = Math.min(start + size, reviews.size());
        List<ProductReviewDto> paginatedReviews = reviews.subList(
                Math.min(start, reviews.size()),
                Math.min(end, reviews.size())
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "All reviews retrieved successfully", paginatedReviews));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ProductReviewDto>>> getPendingReviews() {
        List<ProductReviewDto> reviews = productReviewService.getPendingReviews();
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending reviews retrieved successfully", reviews));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductReviewDto>>> getAllProductReviews(@PathVariable Long productId) {
        List<ProductReviewDto> reviews = productReviewService.getAllReviewsByProductId(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "All product reviews retrieved successfully", reviews));
    }

    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<ApiResponse<ProductReviewDto>> approveReview(@PathVariable Long reviewId) {
        ProductReviewDto review = productReviewService.approveReview(reviewId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review approved successfully", review));
    }

    // PUT /api/admin/reviews/{reviewId}/moderate - Moderate review with approval status and notes
    @PutMapping("/{reviewId}/moderate")
    public ResponseEntity<ApiResponse<ProductReviewDto>> moderateReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> moderationRequest) {

        Boolean approved = (Boolean) moderationRequest.get("approved");
        String moderatorNotes = (String) moderationRequest.get("moderatorNotes");

        if (approved == null) {
            approved = false; // Default to not approved
        }

        ProductReviewDto review = productReviewService.moderateReview(reviewId, approved, moderatorNotes);

        String message = approved ? "Review approved successfully" : "Review rejected successfully";
        return ResponseEntity.ok(new ApiResponse<>(true, message, review));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<String>> deleteReview(@PathVariable Long reviewId) {
        productReviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review deleted successfully", null));
    }
}