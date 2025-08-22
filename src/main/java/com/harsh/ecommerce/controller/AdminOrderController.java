package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.*;
import com.harsh.ecommerce.entity.OrderStatus;
import com.harsh.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@CrossOrigin(origins = "*")
@Tag(name = "👨‍💼 Admin - Orders", description = "Order administration (admin only)")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders (Admin)", description = "Retrieves a paginated list of all orders, with optional filtering by status. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<Page<OrderSummaryDto>>> getAllOrders(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by order status", example = "PENDING", schema = @Schema(implementation = OrderStatus.class))
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

        com.harsh.ecommerce.dto.ApiResponse<Page<OrderSummaryDto>> response = new com.harsh.ecommerce.dto.ApiResponse<>(
                true,
                "Orders retrieved successfully",
                orders
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details (Admin)", description = "Retrieves the full details of a specific order by ID. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<OrderDto>> getOrderDetails(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long orderId) {
        OrderDto order = orderService.getOrderByIdForAdmin(orderId);

        com.harsh.ecommerce.dto.ApiResponse<OrderDto> response = new com.harsh.ecommerce.dto.ApiResponse<>(
                true,
                "Order details retrieved successfully",
                order
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status (Admin)", description = "Updates the status of an existing order. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status update or order not found")
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<OrderDto>> updateOrderStatus(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusDto updateDto) {

        OrderDto order = orderService.updateOrderStatus(orderId, updateDto);

        com.harsh.ecommerce.dto.ApiResponse<OrderDto> response = new com.harsh.ecommerce.dto.ApiResponse<>(
                true,
                "Order status updated successfully",
                order
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get order analytics (Admin)", description = "Retrieves key statistics and metrics about orders. Admin only.")
    @ApiResponse(responseCode = "200", description = "Order analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = OrderAnalyticsDto.class)))
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<OrderAnalyticsDto>> getOrderAnalytics() {
        OrderAnalyticsDto analytics = orderService.getOrderAnalytics();

        com.harsh.ecommerce.dto.ApiResponse<OrderAnalyticsDto> response = new com.harsh.ecommerce.dto.ApiResponse<>(
                true,
                "Order analytics retrieved successfully",
                analytics
        );

        return ResponseEntity.ok(response);
    }
}