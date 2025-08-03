package com.harsh.ecommerce.repository;

import com.harsh.ecommerce.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Basic queries
    Optional<Category> findByName(String name);
    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);

    // Status queries
    List<Category> findByIsActiveTrue();
    Page<Category> findByIsActive(Boolean isActive, Pageable pageable);

    // Search queries
    @Query("SELECT c FROM Category c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Category> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Ordering queries
    List<Category> findByIsActiveTrueOrderBySortOrderAscNameAsc();

    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    Page<Category> findActiveCategories(Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(c) FROM Category c WHERE c.isActive = true")
    long countActiveCategories();

    @Query("SELECT c FROM Category c LEFT JOIN c.products p WHERE c.isActive = true GROUP BY c ORDER BY COUNT(p) DESC")
    List<Category> findCategoriesByProductCount();
}