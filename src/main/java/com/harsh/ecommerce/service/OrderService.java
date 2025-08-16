package com.harsh.ecommerce.service;

import com.harsh.ecommerce.dto.*;
import com.harsh.ecommerce.entity.*;
import com.harsh.ecommerce.exception.InsufficientStockException;
import com.harsh.ecommerce.exception.UserNotFoundException;
import com.harsh.ecommerce.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    public void updateOrderStatus(Long orderId, OrderStatus status) {
        updateOrderStatus(orderId, new UpdateOrderStatusDto(status));
    }

    public OrderDto createOrderFromCart(String userEmail, CreateOrderDto createOrderDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        Long userId = user.getId();

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found or empty"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cannot create order from empty cart");
        }

        validateCartStock(cart);

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        order.setShippingAddress(createOrderDto.getShippingAddress());
        order.setShippingCity(createOrderDto.getShippingCity());
        order.setShippingState(createOrderDto.getShippingState());
        order.setShippingPostalCode(createOrderDto.getShippingPostalCode());
        order.setShippingCountry(createOrderDto.getShippingCountry());

        order.setPaymentMethod(createOrderDto.getPaymentMethod());
        order.setNotes(createOrderDto.getNotes());

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStock()
                );
            }

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), cartItem.getUnitPrice());
            order.addOrderItem(orderItem);

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.calculateTotals();

        order = orderRepository.save(order);

        cartService.clearCart(userId);

        return convertToOrderDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return convertToOrderDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return convertToOrderDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getUserOrders(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return orders.map(this::convertToOrderSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToOrderSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return orders.map(this::convertToOrderSummaryDto);
    }

    public OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusDto updateDto) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(updateDto.getStatus());

        switch (updateDto.getStatus()) {
            case SHIPPED:
                order.markAsShipped();
                break;
            case DELIVERED:
                order.markAsDelivered();
                break;
            case CANCELLED:
                handleOrderCancellation(order);
                break;
            default:
                break;
        }

        if (updateDto.getNotes() != null) {
            order.setNotes(updateDto.getNotes());
        }

        order = orderRepository.save(order);

        return convertToOrderDto(order);
    }

    public void cancelOrder(Long orderId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.canBeCancelled()) {
            throw new RuntimeException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        handleOrderCancellation(order);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public void updatePaymentStatus(Long orderId, PaymentStatus status) {
        updatePaymentStatus(orderId, new UpdatePaymentStatusDto(status));
    }

    public OrderDto updatePaymentStatus(Long orderId, UpdatePaymentStatusDto updateDto) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus(updateDto.getStatus());

        if (updateDto.getNotes() != null) {
            order.setNotes(updateDto.getNotes());
        }

        order = orderRepository.save(order);
        return convertToOrderDto(order);
    }

    @Transactional(readOnly = true)
    public OrderAnalyticsDto getOrderAnalytics() {
        Long totalOrders = orderRepository.countTotalOrders();
        Long pendingOrders = orderRepository.countOrdersByStatus(OrderStatus.PENDING);
        Long processingOrders = orderRepository.countOrdersByStatus(OrderStatus.PROCESSING);
        Long shippedOrders = orderRepository.countOrdersByStatus(OrderStatus.SHIPPED);
        Long deliveredOrders = orderRepository.countOrdersByStatus(OrderStatus.DELIVERED);
        Long cancelledOrders = orderRepository.countOrdersByStatus(OrderStatus.CANCELLED);

        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        BigDecimal averageOrderValue = orderRepository.getAverageOrderValue();
        Long totalCustomers = orderRepository.countUniqueCustomers();

        return new OrderAnalyticsDto(
                totalOrders != null ? totalOrders : 0L,
                pendingOrders != null ? pendingOrders : 0L,
                processingOrders != null ? processingOrders : 0L,
                shippedOrders != null ? shippedOrders : 0L,
                deliveredOrders != null ? deliveredOrders : 0L,
                cancelledOrders != null ? cancelledOrders : 0L,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO,
                totalCustomers != null ? totalCustomers : 0L
        );
    }

    private void validateCartStock(Cart cart) {
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStock() +
                                ", Required: " + cartItem.getQuantity()
                );
            }
        }
    }

    private void handleOrderCancellation(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
            productRepository.save(product);
        }
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + randomPart;
    }

    private OrderDto convertToOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setTotalItems(order.getTotalItems());

        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::convertToOrderItemDto)
                .collect(Collectors.toList());
        dto.setOrderItems(orderItemDtos);

        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingCity(order.getShippingCity());
        dto.setShippingState(order.getShippingState());
        dto.setShippingPostalCode(order.getShippingPostalCode());
        dto.setShippingCountry(order.getShippingCountry());

        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentTransactionId(order.getPaymentTransactionId());

        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setShippedAt(order.getShippedAt());
        dto.setDeliveredAt(order.getDeliveredAt());

        dto.setNotes(order.getNotes());

        return dto;
    }

    private OrderSummaryDto convertToOrderSummaryDto(Order order) {
        OrderSummaryDto dto = new OrderSummaryDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setTotalItems(order.getTotalItems());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        return dto;
    }

    private OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProductName());
        dto.setProductDescription(orderItem.getProductDescription());
        dto.setProductImageUrl(orderItem.getProductImageUrl());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setSubtotal(orderItem.getSubtotal());

        return dto;
    }
}
