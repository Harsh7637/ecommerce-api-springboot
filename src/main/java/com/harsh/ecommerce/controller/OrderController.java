package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.*;
import com.harsh.ecommerce.entity.OrderStatus;
import com.harsh.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(@Valid @RequestBody CreateOrderDto createOrderDto) {
        String userEmail = getCurrentUserEmail();
        OrderDto order = orderService.createOrderFromCart(userEmail, createOrderDto);

        return ResponseEntity.ok(
                ApiResponse.<OrderDto>builder()
                        .success(true)
                        .message("Order created successfully")
                        .data(order)
                        .build()
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(@PathVariable Long orderId) {
        String userEmail = getCurrentUserEmail();
        OrderDto order = orderService.getOrderById(orderId, userEmail);

        return ResponseEntity.ok(
                ApiResponse.<OrderDto>builder()
                        .success(true)
                        .message("Order retrieved successfully")
                        .data(order)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryDto>>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String userEmail = getCurrentUserEmail();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderSummaryDto> orders = orderService.getUserOrders(userEmail, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<OrderSummaryDto>>builder()
                        .success(true)
                        .message("Orders retrieved successfully")
                        .data(orders)
                        .build()
        );
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long orderId) {
        String userEmail = getCurrentUserEmail();
        orderService.cancelOrder(orderId, userEmail);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .build()
        );
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
