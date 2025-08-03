package com.harsh.ecommerce.specification;

import com.harsh.ecommerce.entity.Category;
import com.harsh.ecommerce.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> hasNameOrDescriptionLike(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), likePattern)
            );
        };
    }

    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasCategory(Category category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Product> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
    }

    public static Specification<Product> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> hasStockGreaterThan(Integer minStock) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("stockQuantity"), minStock);
    }

    public static Specification<Product> hasStockLessThan(Integer maxStock) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("stockQuantity"), maxStock);
    }

    public static Specification<Product> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isActive"));
    }

    public static Specification<Product> isFeatured() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isFeatured"));
    }

    public static Specification<Product> isInStock() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("stockQuantity"), 0);
    }

    public static Specification<Product> isOutOfStock() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("stockQuantity"), 0);
    }

    public static Specification<Product> hasWeightBetween(BigDecimal minWeight, BigDecimal maxWeight) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("weight"), minWeight, maxWeight);
    }
}
