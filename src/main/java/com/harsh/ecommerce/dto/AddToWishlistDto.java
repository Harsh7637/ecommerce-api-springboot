package com.harsh.ecommerce.dto;

public class AddToWishlistDto {
    private Long productId;

    public AddToWishlistDto() {}

    public AddToWishlistDto(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}