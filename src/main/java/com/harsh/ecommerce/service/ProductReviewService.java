package com.harsh.ecommerce.service;

import com.harsh.ecommerce.dto.CreateReviewDto;
import com.harsh.ecommerce.dto.ProductReviewDto;
import com.harsh.ecommerce.dto.ReviewStatsDto;
import com.harsh.ecommerce.entity.Product;
import com.harsh.ecommerce.entity.ProductReview;
import com.harsh.ecommerce.entity.User;
import com.harsh.ecommerce.exception.*;
import com.harsh.ecommerce.repository.ProductRepository;
import com.harsh.ecommerce.repository.ProductReviewRepository;
import com.harsh.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductReviewService {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @CacheEvict(value = {"productReviews", "reviewStats"}, key = "#productId")
    public ProductReviewDto createReview(Long productId, Long userId, CreateReviewDto createReviewDto) {
        if (createReviewDto.getRating() < 1 || createReviewDto.getRating() > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5");
        }

        // Validate that product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (productReviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new DuplicateReviewException("User has already reviewed this product");
        }

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setTitle(createReviewDto.getTitle()); // Set title
        review.setComment(createReviewDto.getComment());
        review.setRating(createReviewDto.getRating());
        review.setApproved(true); // Auto-approve for now (change to false if you want moderation)

        ProductReview savedReview = productReviewRepository.save(review);
        return convertToDto(savedReview);
    }

    @Cacheable(value = "productReviews", key = "#productId")
    public List<ProductReviewDto> getApprovedReviewsByProductId(Long productId) {
        // Validate that product exists first
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }

        List<ProductReview> reviews = productReviewRepository.findByProductIdAndApprovedTrue(productId);
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProductReviewDto> getAllReviewsByProductId(Long productId) {
        // Validate that product exists first
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }

        List<ProductReview> reviews = productReviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProductReviewDto> getPendingReviews() {
        List<ProductReview> reviews = productReviewRepository.findByApprovedFalse();
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProductReviewDto> getAllReviews() {
        List<ProductReview> reviews = productReviewRepository.findAll();
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"productReviews", "reviewStats"}, allEntries = true)
    public ProductReviewDto approveReview(Long reviewId) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        review.setApproved(true);
        ProductReview savedReview = productReviewRepository.save(review);
        return convertToDto(savedReview);
    }

    @CacheEvict(value = {"productReviews", "reviewStats"}, allEntries = true)
    public ProductReviewDto moderateReview(Long reviewId, boolean approved, String moderatorNotes) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        review.setApproved(approved);
        // Note: You might want to add a moderatorNotes field to the entity
        ProductReview savedReview = productReviewRepository.save(review);
        return convertToDto(savedReview);
    }

    @CacheEvict(value = {"productReviews", "reviewStats"}, allEntries = true)
    public void deleteReview(Long reviewId, Long userId) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only delete your own reviews");
        }

        productReviewRepository.delete(review);
    }

    @CacheEvict(value = {"productReviews", "reviewStats"}, allEntries = true)
    public void deleteReviewByAdmin(Long reviewId) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        productReviewRepository.delete(review);
    }

    @Cacheable(value = "reviewStats", key = "#productId")
    public ReviewStatsDto getReviewStats(Long productId) {
        // Validate that product exists first
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }

        ReviewStatsDto stats = new ReviewStatsDto();

        Long totalReviews = productReviewRepository.countByProductIdAndApprovedTrue(productId);
        stats.setTotalReviews(totalReviews);

        if (totalReviews > 0) {
            Double averageRating = productReviewRepository.findAverageRatingByProductId(productId);
            stats.setAverageRating(averageRating != null ? averageRating : 0.0);

            stats.setFiveStarReviews(productReviewRepository.countByProductIdAndRatingAndApprovedTrue(productId, 5));
            stats.setFourStarReviews(productReviewRepository.countByProductIdAndRatingAndApprovedTrue(productId, 4));
            stats.setThreeStarReviews(productReviewRepository.countByProductIdAndRatingAndApprovedTrue(productId, 3));
            stats.setTwoStarReviews(productReviewRepository.countByProductIdAndRatingAndApprovedTrue(productId, 2));
            stats.setOneStarReviews(productReviewRepository.countByProductIdAndRatingAndApprovedTrue(productId, 1));
        } else {
            // Set default values when no reviews
            stats.setAverageRating(0.0);
            stats.setFiveStarReviews(0L);
            stats.setFourStarReviews(0L);
            stats.setThreeStarReviews(0L);
            stats.setTwoStarReviews(0L);
            stats.setOneStarReviews(0L);
        }

        return stats;
    }

    @CacheEvict(value = {"productReviews", "reviewStats"}, allEntries = true)
    public ProductReviewDto updateReview(Long reviewId, Long userId, CreateReviewDto updateReviewDto) {
        if (updateReviewDto.getRating() < 1 || updateReviewDto.getRating() > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5");
        }

        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only update your own reviews");
        }

        review.setTitle(updateReviewDto.getTitle());
        review.setComment(updateReviewDto.getComment());
        review.setRating(updateReviewDto.getRating());
        // Keep approved status unchanged when updating

        ProductReview savedReview = productReviewRepository.save(review);
        return convertToDto(savedReview);
    }

    private ProductReviewDto convertToDto(ProductReview review) {
        ProductReviewDto dto = new ProductReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getEmail());
        dto.setTitle(review.getTitle()); // Include title
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setApproved(review.isApproved());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}