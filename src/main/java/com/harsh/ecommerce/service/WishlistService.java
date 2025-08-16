package com.harsh.ecommerce.service;

import com.harsh.ecommerce.dto.AddToCartDto;
import com.harsh.ecommerce.dto.AddToWishlistDto;
import com.harsh.ecommerce.dto.WishlistDto;
import com.harsh.ecommerce.dto.WishlistItemDto;
import com.harsh.ecommerce.entity.*;
import com.harsh.ecommerce.exception.*;
import com.harsh.ecommerce.repository.ProductRepository;
import com.harsh.ecommerce.repository.UserRepository;
import com.harsh.ecommerce.repository.WishlistItemRepository;
import com.harsh.ecommerce.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    public WishlistDto getWishlistByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    return wishlistRepository.save(newWishlist);
                });

        return convertToDto(wishlist);
    }

    public WishlistDto addToWishlist(Long userId, AddToWishlistDto addToWishlistDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Product product = productRepository.findById(addToWishlistDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    return wishlistRepository.save(newWishlist);
                });

        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), product.getId())) {
            throw new DuplicateItemException("Product already exists in wishlist");
        }

        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setWishlist(wishlist);
        wishlistItem.setProduct(product);
        wishlistItemRepository.save(wishlistItem);

        // Refresh the wishlist to get updated items
        wishlist = wishlistRepository.findById(wishlist.getId()).orElse(wishlist);

        return convertToDto(wishlist);
    }

    public void removeFromWishlist(Long userId, Long productId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found"));

        if (!wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new WishlistItemNotFoundException("Product not found in wishlist");
        }

        wishlistItemRepository.deleteByWishlistIdAndProductId(wishlist.getId(), productId);
    }

    public void clearWishlist(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found"));

        // Handle null wishlistItems
        if (wishlist.getWishlistItems() != null && !wishlist.getWishlistItems().isEmpty()) {
            wishlistItemRepository.deleteAll(wishlist.getWishlistItems());
        }
    }

    // NEW METHOD: Move from Wishlist to Cart
    public void moveToCart(Long userId, Long productId, Integer quantity) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Get wishlist
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found"));

        // Check if product exists in wishlist
        if (!wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new WishlistItemNotFoundException("Product not found in wishlist");
        }

        // Create AddToCartDto
        AddToCartDto addToCartDto = new AddToCartDto();
        addToCartDto.setProductId(productId);
        addToCartDto.setQuantity(quantity);

        try {
            // Add to cart first
            cartService.addToCart(userId, addToCartDto);

            // If successful, remove from wishlist
            removeFromWishlist(userId, productId);

        } catch (InsufficientStockException e) {
            // Re-throw the stock exception for proper error handling
            throw e;
        } catch (Exception e) {
            // If cart addition fails, don't remove from wishlist
            throw new RuntimeException("Failed to move product to cart: " + e.getMessage(), e);
        }
    }

    private WishlistDto convertToDto(Wishlist wishlist) {
        WishlistDto dto = new WishlistDto();
        dto.setId(wishlist.getId());
        dto.setUserId(wishlist.getUser().getId());
        dto.setCreatedAt(wishlist.getCreatedAt());
        dto.setUpdatedAt(wishlist.getUpdatedAt());

        // Handle null or empty wishlistItems safely
        List<WishlistItemDto> itemDtos;
        if (wishlist.getWishlistItems() != null) {
            itemDtos = wishlist.getWishlistItems().stream()
                    .map(this::convertItemToDto)
                    .collect(Collectors.toList());
        } else {
            itemDtos = Collections.emptyList();
        }
        dto.setItems(itemDtos);

        return dto;
    }

    private WishlistItemDto convertItemToDto(WishlistItem item) {
        WishlistItemDto dto = new WishlistItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());

        // Use single main image for quick display
        dto.setProductImage(item.getProduct().getMainImage());

        // Include all images for detailed view if needed
        dto.setProductImages(item.getProduct().getImages());

        dto.setProductPrice(item.getProduct().getPrice().doubleValue());
        dto.setAddedAt(item.getAddedAt());
        return dto;
    }
}