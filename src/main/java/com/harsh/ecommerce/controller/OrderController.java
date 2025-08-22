package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.*;
import com.harsh.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Tag(name = "👤 User - Orders", description = "Order management (requires authentication)")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order from the cart", description = "Creates a new order for the authenticated user from their current cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or empty cart",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<OrderDto>> createOrder(@Valid @RequestBody CreateOrderDto createOrderDto) {
        try {
            String userEmail = getCurrentUserEmail();
            OrderDto order = orderService.createOrderFromCart(userEmail, createOrderDto);
            return ResponseEntity.ok(
                    com.harsh.ecommerce.dto.ApiResponse.<OrderDto>builder()
                            .success(true)
                            .message("Order created successfully")
                            .data(order)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.harsh.ecommerce.dto.ApiResponse.<OrderDto>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get a specific order by ID", description = "Retrieves a single order for the authenticated user by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found or not accessible by user",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<OrderDto>> getOrder(
            @Parameter(description = "ID of the order to retrieve", example = "1", required = true)
            @PathVariable Long orderId) {
        try {
            String userEmail = getCurrentUserEmail();
            OrderDto order = orderService.getOrderById(orderId, userEmail);
            return ResponseEntity.ok(
                    com.harsh.ecommerce.dto.ApiResponse.<OrderDto>builder()
                            .success(true)
                            .message("Order retrieved successfully")
                            .data(order)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.harsh.ecommerce.dto.ApiResponse.<OrderDto>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping
    @Operation(summary = "Get user's order history", description = "Retrieves a paginated list of all orders for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<Page<OrderSummaryDto>>> getUserOrders(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "createdAt", schema = @Schema(allowableValues = {"createdAt", "totalPrice"}))
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc", schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            String userEmail = getCurrentUserEmail();
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<OrderSummaryDto> orders = orderService.getUserOrders(userEmail, pageable);
            return ResponseEntity.ok(
                    com.harsh.ecommerce.dto.ApiResponse.<Page<OrderSummaryDto>>builder()
                            .success(true)
                            .message("Orders retrieved successfully")
                            .data(orders)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.harsh.ecommerce.dto.ApiResponse.<Page<OrderSummaryDto>>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order", description = "Allows an authenticated user to cancel their own order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid order ID or order cannot be cancelled",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "User is not authorized to cancel this order",
                    content = @Content(schema = @Schema(implementation = com.harsh.ecommerce.dto.ApiResponse.class)))
    })
    public ResponseEntity<com.harsh.ecommerce.dto.ApiResponse<Void>> cancelOrder(
            @Parameter(description = "ID of the order to cancel", example = "1", required = true)
            @PathVariable Long orderId) {
        try {
            String userEmail = getCurrentUserEmail();
            orderService.cancelOrder(orderId, userEmail);
            return ResponseEntity.ok(
                    com.harsh.ecommerce.dto.ApiResponse.<Void>builder()
                            .success(true)
                            .message("Order cancelled successfully")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.harsh.ecommerce.dto.ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}