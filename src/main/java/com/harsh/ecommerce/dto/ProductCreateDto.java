package com.harsh.ecommerce.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class ProductCreateDto {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 2 decimal places")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String sku;
    private List<String> images;

    @DecimalMin(value = "0.0", message = "Weight must be positive")
    @Digits(integer = 5, fraction = 2, message = "Weight must have at most 2 decimal places")
    private BigDecimal weight;

    @Size(max = 100, message = "Dimensions cannot exceed 100 characters")
    private String dimensions;

    private Boolean isActive = true;
    private Boolean isFeatured = false;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder = 0;

    // Constructors
    public ProductCreateDto() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

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
}