package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.AddToCartDto;
import com.harsh.ecommerce.dto.ApiResponse;
import com.harsh.ecommerce.dto.CartDto;
import com.harsh.ecommerce.dto.UpdateCartItemDto;
import com.harsh.ecommerce.service.CartService;
import com.harsh.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
@Tag(name = "👤 User - Cart", description = "Shopping cart operations (requires authentication)")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart() {
        Long userId = getCurrentUserId();
        CartDto cart = cartService.getCartByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.<CartDto>builder()
                        .success(true)
                        .message("Cart retrieved successfully")
                        .data(cart)
                        .build()
        );
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartDto>> addToCart(@Valid @RequestBody AddToCartDto addToCartDto) {
        Long userId = getCurrentUserId();
        CartDto cart = cartService.addToCart(userId, addToCartDto);

        return ResponseEntity.ok(
                ApiResponse.<CartDto>builder()
                        .success(true)
                        .message("Item added to cart successfully")
                        .data(cart)
                        .build()
        );
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDto>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemDto updateDto) {

        Long userId = getCurrentUserId();
        CartDto cart = cartService.updateCartItem(userId, cartItemId, updateDto);

        return ResponseEntity.ok(
                ApiResponse.<CartDto>builder()
                        .success(true)
                        .message("Cart item updated successfully")
                        .data(cart)
                        .build()
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable Long cartItemId) {
        Long userId = getCurrentUserId();
        cartService.removeFromCart(userId, cartItemId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Item removed from cart successfully")
                        .build()
        );
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        Long userId = getCurrentUserId();
        cartService.clearCart(userId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Cart cleared successfully")
                        .build()
        );
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateCart() {
        Long userId = getCurrentUserId();
        boolean isValid = cartService.validateCartStock(userId);

        if (!isValid) {
            cartService.removeOutOfStockItems(userId);
        }

        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .success(true)
                        .message(isValid ? "Cart is valid" : "Out of stock items removed from cart")
                        .data(isValid)
                        .build()
        );
    }

    // FIXED METHOD - This was the problem!
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Get email from JWT token
        String email = authentication.getName();

        // Use UserService to get the actual User entity and extract ID
        return userService.getUserByEmail(email).getId();
    }
}