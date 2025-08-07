package com.harsh.ecommerce.service;

import com.harsh.ecommerce.dto.AddToCartDto;
import com.harsh.ecommerce.dto.CartDto;
import com.harsh.ecommerce.dto.CartItemDto;
import com.harsh.ecommerce.dto.UpdateCartItemDto;
import com.harsh.ecommerce.entity.*;
import com.harsh.ecommerce.exception.InsufficientStockException;
import com.harsh.ecommerce.exception.ProductNotFoundException;
import com.harsh.ecommerce.exception.UserNotFoundException;
import com.harsh.ecommerce.repository.CartItemRepository;
import com.harsh.ecommerce.repository.CartRepository;
import com.harsh.ecommerce.repository.ProductRepository;
import com.harsh.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartDto getCartByUserId(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return convertToCartDto(cart);
    }

    public CartDto addToCart(Long userId, AddToCartDto addToCartDto) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate product
        Product product = productRepository.findById(addToCartDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Check stock availability
        if (product.getStock() < addToCartDto.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock. Available: " + product.getStock());
        }

        // Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Check if product already exists in cart
        CartItem existingCartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (existingCartItem != null) {
            // Update existing cart item
            int newQuantity = existingCartItem.getQuantity() + addToCartDto.getQuantity();

            // Check total quantity against stock
            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException("Insufficient stock. Available: " + product.getStock() +
                        ", Already in cart: " + existingCartItem.getQuantity());
            }

            existingCartItem.setQuantity(newQuantity);
            cartItemRepository.save(existingCartItem);
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem(cart, product, addToCartDto.getQuantity(), product.getPrice());
            cart.addCartItem(cartItem);
            cartItemRepository.save(cartItem);
        }

        cart.recalculateCart();
        cartRepository.save(cart);

        log.info("Added {} {} to cart for user {}", addToCartDto.getQuantity(), product.getName(), userId);
        return convertToCartDto(cart);
    }

    public CartDto updateCartItem(Long userId, Long cartItemId, UpdateCartItemDto updateDto) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to user");
        }

        // Check stock availability
        if (cartItem.getProduct().getStock() < updateDto.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock. Available: " + cartItem.getProduct().getStock());
        }

        cartItem.setQuantity(updateDto.getQuantity());
        cartItemRepository.save(cartItem);

        cart.recalculateCart();
        cartRepository.save(cart);

        log.info("Updated cart item {} quantity to {} for user {}", cartItemId, updateDto.getQuantity(), userId);
        return convertToCartDto(cart);
    }

    public void removeFromCart(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to user");
        }

        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.recalculateCart();
        cartRepository.save(cart);

        log.info("Removed cart item {} from cart for user {}", cartItemId, userId);
    }

    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);

        cartItemRepository.deleteByCartId(cart.getId());
        cart.clearCart();
        cartRepository.save(cart);

        log.info("Cleared cart for user {}", userId);
    }

    public boolean validateCartStock(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId).orElse(null);
        if (cart == null || cart.getCartItems().isEmpty()) {
            return true;
        }

        return cart.getCartItems().stream()
                .allMatch(item -> item.getProduct().getStock() >= item.getQuantity());
    }

    public void removeOutOfStockItems(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId).orElse(null);
        if (cart == null) {
            return;
        }

        List<CartItem> outOfStockItems = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getStock() < item.getQuantity())
                .collect(Collectors.toList());

        for (CartItem item : outOfStockItems) {
            cart.removeCartItem(item);
            cartItemRepository.delete(item);
        }

        if (!outOfStockItems.isEmpty()) {
            cart.recalculateCart();
            cartRepository.save(cart);
            log.info("Removed {} out of stock items from cart for user {}", outOfStockItems.size(), userId);
        }
    }

    // Helper methods
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    private Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setTotalItems(0);

        return cartRepository.save(cart);
    }

    private CartDto convertToCartDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getCartItems().stream()
                .map(this::convertToCartItemDto)
                .collect(Collectors.toList());

        CartDto cartDto = new CartDto();
        cartDto.setId(cart.getId());
        cartDto.setItems(itemDtos);
        cartDto.setTotalAmount(cart.getTotalAmount());
        cartDto.setTotalItems(cart.getTotalItems());
        cartDto.setUpdatedAt(cart.getUpdatedAt());

        return cartDto;
    }

    private CartItemDto convertToCartItemDto(CartItem cartItem) {
        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductImageUrl(cartItem.getProduct().getMainImage());
        dto.setUnitPrice(cartItem.getUnitPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setSubtotal(cartItem.getSubtotal());
        dto.setAvailableStock(cartItem.getProduct().getStock());
        dto.setAddedAt(cartItem.getCreatedAt());

        return dto;
    }
}