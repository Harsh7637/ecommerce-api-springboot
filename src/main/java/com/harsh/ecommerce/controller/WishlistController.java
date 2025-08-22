package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.AddToWishlistDto;
import com.harsh.ecommerce.dto.WishlistDto;
import com.harsh.ecommerce.entity.User;
import com.harsh.ecommerce.service.WishlistService;
import com.harsh.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
@Tag(name = "👤 User - Wishlist", description = "Wishlist management (requires authentication)")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get user's wishlist", description = "Retrieves the entire wishlist for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully",
            content = @Content(schema = @Schema(implementation = WishlistDto.class)))
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<WishlistDto>> getWishlist() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // This is the email
        User user = userService.findByEmail(userEmail);
        Long userId = user.getId();

        WishlistDto wishlist = wishlistService.getWishlistByUserId(userId);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Wishlist retrieved successfully", wishlist));
    }

    @PostMapping("/add")
    @Operation(summary = "Add a product to wishlist", description = "Adds a product to the authenticated user's wishlist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to wishlist successfully",
                    content = @Content(schema = @Schema(implementation = WishlistDto.class))),
            @ApiResponse(responseCode = "400", description = "Product already in wishlist or invalid request",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<WishlistDto>> addToWishlist(@RequestBody AddToWishlistDto addToWishlistDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // This is the email
        User user = userService.findByEmail(userEmail);
        Long userId = user.getId();

        WishlistDto wishlist = wishlistService.addToWishlist(userId, addToWishlistDto);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Product added to wishlist successfully", wishlist));
    }

    @DeleteMapping("/remove/{productId}")
    @Operation(summary = "Remove a product from wishlist", description = "Removes a product from the authenticated user's wishlist by product ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product removed from wishlist successfully",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found in wishlist",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<String>> removeFromWishlist(@PathVariable Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // This is the email
        User user = userService.findByEmail(userEmail);
        Long userId = user.getId();

        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Product removed from wishlist successfully", null));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear wishlist", description = "Removes all products from the authenticated user's wishlist.")
    @ApiResponse(responseCode = "200", description = "Wishlist cleared successfully",
            content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<String>> clearWishlist() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // This is the email
        User user = userService.findByEmail(userEmail);
        Long userId = user.getId();

        wishlistService.clearWishlist(userId);
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Wishlist cleared successfully", null));
    }

    // NEW ENDPOINT: Move from Wishlist to Cart
    @PostMapping("/move-to-cart/{productId}")
    @Operation(summary = "Move product to cart", description = "Moves a product from the wishlist to the shopping cart. If it's already in the cart, it increments the quantity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product moved to cart successfully",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Product is out of stock or other issue",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<String>> moveToCart(
            @Parameter(description = "ID of the product to move", example = "1", required = true)
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
        return ResponseEntity.ok(new com.harsh.ecommerce.dto.ApiResponse<>(true, "Product moved to cart successfully", null));
    }
}
