package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.AddToWishlistDto;
import com.harsh.ecommerce.dto.ApiResponse;
import com.harsh.ecommerce.dto.WishlistDto;
import com.harsh.ecommerce.entity.User;
import com.harsh.ecommerce.service.WishlistService;
import com.harsh.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<WishlistDto>> getWishlist() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // This is the email
        User user = userService.findByEmail(userEmail);
        Long userId = user.getId();

        WishlistDto wishlist = wishlistService.getWishlistByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist retrieved successfully", wishlist));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WishlistDto>> addToWishlist(@RequestBody AddToWishlistDto addToWishlistDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // This is the email
        User user = userService.findByEmail(userEmail);
        Long userId = user.getId();

        WishlistDto wishlist = wishlistService.addToWishlist(userId, addToWishlistDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product added to wishlist successfully", wishlist));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(@PathVariable Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // This is the email
        User user = userService.findByEmail(userEmail);
        Long userId = user.getId();

        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product removed from wishlist successfully", null));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearWishlist() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // This is the email
        User user = userService.findByEmail(userEmail);
        Long userId = user.getId();

        wishlistService.clearWishlist(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist cleared successfully", null));
    }

    // NEW ENDPOINT: Move from Wishlist to Cart
    @PostMapping("/move-to-cart/{productId}")
    public ResponseEntity<ApiResponse<String>> moveToCart(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userService.findByEmail(userEmail);
        Long userId = user.getId();

        Integer quantity = request.get("quantity");
        if (quantity == null || quantity <= 0) {
            quantity = 1; // default quantity
        }

        wishlistService.moveToCart(userId, productId, quantity);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product moved to cart successfully", null));
    }
}