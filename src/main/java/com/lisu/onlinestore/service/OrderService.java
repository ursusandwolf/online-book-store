package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.order.OrderDto;
import com.lisu.onlinestore.dto.order.OrderItemDto;
import com.lisu.onlinestore.dto.order.request.CreateOrderRequestDto;
import com.lisu.onlinestore.dto.order.request.UpdateOrderStatusRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto placeOrder(Long userId, CreateOrderRequestDto request);

    Page<OrderDto> getOrders(Long userId, Pageable pageable);

    Page<OrderItemDto> getOrderItems(Long userId, Long orderId, Pageable pageable);

    OrderItemDto getOrderItem(Long userId, Long orderId, Long itemId);

    OrderDto updateStatus(Long orderId, UpdateOrderStatusRequestDto request);
}
