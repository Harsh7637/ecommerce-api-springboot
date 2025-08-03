package com.harsh.ecommerce.repository;

import com.harsh.ecommerce.entity.Category;
import com.harsh.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // Basic queries
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySku(String sku);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);

    // Category queries
    List<Product> findByCategory(Category category);
    Page<Product> findByCategory(Category category, Pageable pageable);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Status queries
    List<Product> findByIsActiveTrue();
    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);
    Page<Product> findByIsActiveTrueAndCategory(Category category, Pageable pageable);

    // Featured products
    List<Product> findByIsFeaturedTrueAndIsActiveTrueOrderBySortOrderAsc();
    Page<Product> findByIsFeaturedTrue(Pageable pageable);

    // Stock queries
    List<Product> findByStockQuantityLessThan(Integer threshold);
    List<Product> findByStockQuantityGreaterThan(Integer minStock);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.stockQuantity > 0 AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // Price queries
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.price >= :minPrice AND p.price <= :maxPrice AND p.isActive = true")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    // Search queries
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchActiveProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Combined filters
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true AND " +
            "p.category.id = :categoryId AND " +
            "p.price >= :minPrice AND p.price <= :maxPrice")
    Page<Product> findByCategoryAndPriceRange(@Param("categoryId") Long categoryId,
                                              @Param("minPrice") BigDecimal minPrice,
                                              @Param("maxPrice") BigDecimal maxPrice,
                                              Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    long countActiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    long countProductsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal getAveragePrice();

    @Query("SELECT SUM(p.stockQuantity) FROM Product p WHERE p.isActive = true")
    Long getTotalStock();

    // Top products queries
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Page<Product> findLatestProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.sortOrder ASC, p.name ASC")
    Page<Product> findAllActiveProductsSorted(Pageable pageable);
}