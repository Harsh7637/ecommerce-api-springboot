package com.harsh.ecommerce.dto;

import com.harsh.ecommerce.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductResponseDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String sku;
    private String slug;
    private List<String> images;
    private BigDecimal weight;
    private String dimensions;
    private Boolean isActive;
    private Boolean isFeatured;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Category info
    private Long categoryId;
    private String categoryName;
    private String categorySlug;

    // Stock status
    private Boolean inStock;
    private String stockStatus;

    // Constructors
    public ProductResponseDto() {}

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.sku = product.getSku();
        this.slug = product.getSlug();
        this.images = product.getImages();
        this.weight = product.getWeight();
        this.dimensions = product.getDimensions();
        this.isActive = product.getIsActive();
        this.isFeatured = product.getIsFeatured();
        this.sortOrder = product.getSortOrder();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();

        if (product.getCategory() != null) {
            this.categoryId = product.getCategory().getId();
            this.categoryName = product.getCategory().getName();
            this.categorySlug = product.getCategory().getSlug();
        }

        this.inStock = product.isInStock();
        this.stockStatus = getStockStatusText(product.getStockQuantity());
    }

    private String getStockStatusText(Integer stock) {
        if (stock == null || stock == 0) {
            return "Out of Stock";
        } else if (stock <= 5) {
            return "Low Stock";
        } else {
            return "In Stock";
        }
    }

    public String getMainImage() {
        return images != null && !images.isEmpty() ? images.get(0) : null;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategorySlug() { return categorySlug; }
    public void setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
}