package com.lisu.onlinestore.controller;

import com.lisu.onlinestore.dto.order.OrderDto;
import com.lisu.onlinestore.dto.order.OrderItemDto;
import com.lisu.onlinestore.dto.order.request.CreateOrderRequestDto;
import com.lisu.onlinestore.dto.order.request.UpdateOrderStatusRequestDto;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Order management", description = "Endpoints for managing user orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place an order", description = "Creates an order from the current "
            + "user shopping cart and clears the cart after successful checkout")
    @ApiResponse(responseCode = "201", description = "Order created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or empty shopping cart")
    @ApiResponse(responseCode = "404", description = "User or shopping cart not found")
    public OrderDto placeOrder(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateOrderRequestDto request) {
        return orderService.placeOrder(user.getId(), request);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get order history", description = "Returns paginated order history "
            + "for the current user")
    @ApiResponse(responseCode = "200", description = "Order history retrieved successfully")
    public Page<OrderDto> getOrders(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return orderService.getOrders(user.getId(), pageable);
    }

    @GetMapping("/{orderId}/items")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get order items", description = "Returns paginated items for a "
            + "specific order owned by the current user")
    @ApiResponse(responseCode = "200", description = "Order items retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public Page<OrderItemDto> getOrderItems(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return orderService.getOrderItems(user.getId(), orderId, pageable);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get a specific order item", description = "Returns a single item from "
            + "an order owned by the current user")
    @ApiResponse(responseCode = "200", description = "Order item retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Order item not found")
    public OrderItemDto getOrderItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        return orderService.getOrderItem(user.getId(), orderId, itemId);
    }

    @PatchMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status", description = "Updates the status of an order")
    @ApiResponse(responseCode = "200", description = "Order status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status update request")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public OrderDto updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody @Valid UpdateOrderStatusRequestDto request) {
        return orderService.updateStatus(orderId, request);
    }
}
