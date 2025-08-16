package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.*;
import com.harsh.ecommerce.entity.OrderStatus;
import com.harsh.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryDto>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) OrderStatus status) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderSummaryDto> orders;
        if (status != null) {
            orders = orderService.getOrdersByStatus(status, pageable);
        } else {
            orders = orderService.getAllOrders(pageable);
        }

        ApiResponse<Page<OrderSummaryDto>> response = new ApiResponse<>(
                true,
                "Orders retrieved successfully",
                orders
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderDetails(@PathVariable Long orderId) {
        OrderDto order = orderService.getOrderByIdForAdmin(orderId);

        ApiResponse<OrderDto> response = new ApiResponse<>(
                true,
                "Order details retrieved successfully",
                order
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusDto updateDto) {

        OrderDto order = orderService.updateOrderStatus(orderId, updateDto);

        ApiResponse<OrderDto> response = new ApiResponse<>(
                true,
                "Order status updated successfully",
                order
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<OrderAnalyticsDto>> getOrderAnalytics() {
        OrderAnalyticsDto analytics = orderService.getOrderAnalytics();

        ApiResponse<OrderAnalyticsDto> response = new ApiResponse<>(
                true,
                "Order analytics retrieved successfully",
                analytics
        );

        return ResponseEntity.ok(response);
    }
}