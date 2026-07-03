package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.OrderRepository;
import com.lisu.onlinestore.dao.ShoppingCartRepository;
import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.order.OrderDto;
import com.lisu.onlinestore.dto.order.OrderItemDto;
import com.lisu.onlinestore.dto.order.request.CreateOrderRequestDto;
import com.lisu.onlinestore.dto.order.request.UpdateOrderStatusRequestDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.OrderItemMapper;
import com.lisu.onlinestore.mapper.OrderMapper;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import com.lisu.onlinestore.model.order.Order;
import com.lisu.onlinestore.model.order.OrderItem;
import com.lisu.onlinestore.model.order.OrderStatus;
import com.lisu.onlinestore.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderDto placeOrder(Long userId, CreateOrderRequestDto request) {
        User user = getUserById(userId);
        ShoppingCart shoppingCart = getCartByUser(user);
        validateCartIsNotEmpty(shoppingCart);

        Order order = buildOrder(user, request);
        order.setTotal(addOrderItemsAndCalculateTotal(order, shoppingCart));
        Order savedOrder = orderRepository.save(order);

        clearCart(shoppingCart);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrders(Long userId, Pageable pageable) {
        return orderRepository.findAllByUserId(userId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItemDto> getOrderItems(Long userId, Long orderId, Pageable pageable) {
        return toPage(mapOrderItems(
                getOrderByIdAndUserId(orderId, userId)), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemDto getOrderItem(Long userId, Long orderId, Long itemId) {
        return orderItemMapper.toDto(getOrderItemById(
                getOrderByIdAndUserId(orderId, userId), itemId));
    }

    @Override
    public OrderDto updateStatus(Long orderId, UpdateOrderStatusRequestDto request) {
        Order order = getOrderById(orderId);
        order.setStatus(request.getStatus());
        return orderMapper.toDto(orderRepository.save(order));
    }

    private Order buildOrder(User user, CreateOrderRequestDto request) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(request.getShippingAddress());
        return order;
    }

    private BigDecimal addOrderItemsAndCalculateTotal(Order order, ShoppingCart shoppingCart) {
        return shoppingCart.getCartItems().stream()
                .map(cartItem -> createOrderItem(order, cartItem))
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderItem createOrderItem(Order order, CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setBook(cartItem.getBook());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getBook().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return order.addOrderItem(orderItem);
    }

    private List<OrderItemDto> mapOrderItems(Order order) {
        return order.getOrderItems().stream()
                .sorted(Comparator.comparing(OrderItem::getId))
                .map(orderItemMapper::toDto)
                .toList();
    }

    private Page<OrderItemDto> toPage(List<OrderItemDto> orderItems, Pageable pageable) {
        int start = Math.min((int) pageable.getOffset(), orderItems.size());
        int end = Math.min(start + pageable.getPageSize(), orderItems.size());
        return new PageImpl<>(orderItems.subList(start, end), pageable, orderItems.size());
    }

    private OrderItem getOrderItemById(Order order, Long itemId) {
        return order.getOrderItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Order item not found: " + itemId));
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    private Order getOrderByIdAndUserId(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    private void clearCart(ShoppingCart shoppingCart) {
        shoppingCart.clearCart();
        shoppingCartRepository.save(shoppingCart);
    }

    private ShoppingCart getCartByUser(User user) {
        return shoppingCartRepository.findWithItemsByUser(user)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shopping cart not found for user: " + user.getId()));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private void validateCartIsNotEmpty(ShoppingCart shoppingCart) {
        if (shoppingCart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Shopping cart is empty");
        }
    }
}
