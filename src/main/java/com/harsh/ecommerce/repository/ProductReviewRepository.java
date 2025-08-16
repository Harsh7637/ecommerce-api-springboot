package com.harsh.ecommerce.repository;

import com.harsh.ecommerce.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductIdAndApprovedTrue(Long productId);

    List<ProductReview> findByProductId(Long productId);

    List<ProductReview> findByApprovedFalse();

    Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.approved = true")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.approved = true")
    Long countByProductIdAndApprovedTrue(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.rating = :rating AND r.approved = true")
    Long countByProductIdAndRatingAndApprovedTrue(@Param("productId") Long productId, @Param("rating") int rating);
}