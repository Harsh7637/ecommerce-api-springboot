package com.harsh.ecommerce.exception;

public class WishlistItemNotFoundException extends RuntimeException {
    public WishlistItemNotFoundException(String message) {
        super(message);
    }
}