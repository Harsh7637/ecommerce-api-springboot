package com.harsh.ecommerce.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WishlistItemDto {
    private Long id;
    private Long productId;
    private String productName;

    // Single main image for quick display in wishlist
    private String productImage;

    // Full list of product images
    private List<String> productImages;

    private double productPrice;
    private LocalDateTime addedAt;

    public WishlistItemDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public List<String> getProductImages() { return productImages; }
    public void setProductImages(List<String> productImages) { this.productImages = productImages; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
