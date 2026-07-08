package com.lisu.onlinestore.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lisu.onlinestore.dao.OrderRepository;
import com.lisu.onlinestore.dao.ShoppingCartRepository;
import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.order.OrderItemDto;
import com.lisu.onlinestore.dto.order.OrderDto;
import com.lisu.onlinestore.dto.order.request.CreateOrderRequestDto;
import com.lisu.onlinestore.dto.order.request.UpdateOrderStatusRequestDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.OrderItemMapper;
import com.lisu.onlinestore.mapper.OrderMapper;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import com.lisu.onlinestore.model.order.Order;
import com.lisu.onlinestore.model.order.OrderItem;
import com.lisu.onlinestore.model.order.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @InjectMocks
    private OrderServiceImpl orderService;
    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private User user;
    private ShoppingCart shoppingCart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        Book firstBook = createBook(11L, new BigDecimal("10.00"));
        Book secondBook = createBook(12L, new BigDecimal("15.50"));

        CartItem firstItem = createCartItem(firstBook, 2);
        CartItem secondItem = createCartItem(secondBook, 1);

        shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.addItem(firstItem);
        shoppingCart.addItem(secondItem);
    }

    @Test
    void placeOrder_ShouldCreateOrderAndClearCart() {
        CreateOrderRequestDto request = new CreateOrderRequestDto("Kyiv, Shevchenko ave, 1");
        OrderDto expected = new OrderDto();
        expected.setId(100L);
        expected.setUserId(user.getId());
        expected.setStatus(OrderStatus.PENDING);
        expected.setTotal(new BigDecimal("35.50"));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(shoppingCartRepository.findWithItemsByUser(user)).thenReturn(Optional.of(shoppingCart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(expected);

        OrderDto actual = orderService.placeOrder(user.getId(), request);

        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(expected, actual);
        assertEquals(user, savedOrder.getUser());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(new BigDecimal("35.50"), savedOrder.getTotal());
        assertEquals(request.getShippingAddress(), savedOrder.getShippingAddress());
        assertEquals(2, savedOrder.getOrderItems().size());
        assertTrue(savedOrder.getOrderItems().stream()
                .allMatch(orderItem -> orderItem.getOrder().equals(savedOrder)));
        assertTrue(savedOrder.getOrderItems().stream()
                .map(OrderItem::getPrice)
                .anyMatch(price -> price.compareTo(new BigDecimal("20.00")) == 0));
        assertTrue(savedOrder.getOrderItems().stream()
                .map(OrderItem::getPrice)
                .anyMatch(price -> price.compareTo(new BigDecimal("15.50")) == 0));
        assertTrue(shoppingCart.getCartItems().isEmpty());
        verify(shoppingCartRepository).save(shoppingCart);
    }

    @Test
    void placeOrder_ShouldThrowWhenCartIsEmpty() {
        CreateOrderRequestDto request = new CreateOrderRequestDto("Kyiv, Shevchenko ave, 1");
        ShoppingCart emptyCart = new ShoppingCart();
        emptyCart.setUser(user);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(shoppingCartRepository.findWithItemsByUser(user)).thenReturn(Optional.of(emptyCart));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.placeOrder(user.getId(), request)
        );

        assertEquals("Shopping cart is empty", exception.getMessage());
    }

    @Test
    void placeOrder_ShouldThrowWhenUserNotFound() {
        CreateOrderRequestDto request = new CreateOrderRequestDto("Kyiv, Shevchenko ave, 1");

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.placeOrder(user.getId(), request)
        );

        assertEquals("User not found: 1", exception.getMessage());
    }

    @Test
    void placeOrder_ShouldThrowWhenCartNotFound() {
        CreateOrderRequestDto request = new CreateOrderRequestDto("Kyiv, Shevchenko ave, 1");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(shoppingCartRepository.findWithItemsByUser(user)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.placeOrder(user.getId(), request)
        );

        assertEquals("Shopping cart not found for user: 1", exception.getMessage());
    }

    @Test
    void getOrders_ShouldMapRepositoryPage() {
        Pageable pageable = PageRequest.of(0, 2);
        Order firstOrder = new Order();
        firstOrder.setId(10L);
        Order secondOrder = new Order();
        secondOrder.setId(11L);
        OrderDto firstDto = new OrderDto();
        firstDto.setId(10L);
        OrderDto secondDto = new OrderDto();
        secondDto.setId(11L);
        Page<Order> orders = new PageImpl<>(List.of(firstOrder, secondOrder), pageable, 2);

        when(orderRepository.findAllByUserId(user.getId(), pageable)).thenReturn(orders);
        when(orderMapper.toDto(firstOrder)).thenReturn(firstDto);
        when(orderMapper.toDto(secondOrder)).thenReturn(secondDto);

        Page<OrderDto> actual = orderService.getOrders(user.getId(), pageable);

        assertEquals(2, actual.getTotalElements());
        assertEquals(List.of(firstDto, secondDto), actual.getContent());
    }

    @Test
    void getOrderItems_ShouldReturnSortedAndPagedItems() {
        OrderItem firstItem = createOrderItemWithId(9L, createBook(20L, new BigDecimal("9.00")), 1);
        OrderItem secondItem = createOrderItemWithId(5L, createBook(21L, new BigDecimal("7.00")), 2);
        OrderItem thirdItem = createOrderItemWithId(7L, createBook(22L, new BigDecimal("8.00")), 3);
        Order order = new Order();
        order.setId(10L);
        order.setUser(user);
        order.setOrderItems(Set.of(firstItem, secondItem, thirdItem));
        firstItem.setOrder(order);
        secondItem.setOrder(order);
        thirdItem.setOrder(order);

        OrderItemDto firstDto = new OrderItemDto(5L, 21L, 2);
        OrderItemDto secondDto = new OrderItemDto(7L, 22L, 3);

        when(orderRepository.findByIdAndUserId(10L, user.getId())).thenReturn(Optional.of(order));
        when(orderItemMapper.toDto(secondItem)).thenReturn(firstDto);
        when(orderItemMapper.toDto(thirdItem)).thenReturn(secondDto);

        Page<OrderItemDto> actual = orderService.getOrderItems(user.getId(), 10L, PageRequest.of(0, 2));

        assertEquals(3, actual.getTotalElements());
        assertEquals(List.of(firstDto, secondDto), actual.getContent());
    }

    @Test
    void getOrderItems_ShouldThrowWhenOrderNotFound() {
        when(orderRepository.findByIdAndUserId(10L, user.getId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.getOrderItems(user.getId(), 10L, PageRequest.of(0, 2))
        );

        assertEquals("Order not found: 10", exception.getMessage());
    }

    @Test
    void getOrderItem_ShouldReturnMappedDto() {
        OrderItem item = createOrderItemWithId(99L, createBook(11L, new BigDecimal("10.00")), 2);
        Order order = new Order();
        order.setId(10L);
        order.setUser(user);
        order.setOrderItems(Set.of(item));
        item.setOrder(order);
        OrderItemDto expected = new OrderItemDto(99L, 11L, 2);

        when(orderRepository.findByIdAndUserId(10L, user.getId())).thenReturn(Optional.of(order));
        when(orderItemMapper.toDto(item)).thenReturn(expected);

        OrderItemDto actual = orderService.getOrderItem(user.getId(), 10L, 99L);

        assertEquals(expected, actual);
    }

    @Test
    void getOrderItem_ShouldThrowWhenItemNotBelongToUserOrder() {
        Order order = new Order();
        order.setId(10L);
        order.setUser(user);
        order.setOrderItems(Set.of());

        when(orderRepository.findByIdAndUserId(10L, user.getId())).thenReturn(Optional.of(order));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.getOrderItem(user.getId(), 10L, 99L)
        );

        assertEquals("Order item not found: 99", exception.getMessage());
    }

    @Test
    void updateStatus_ShouldSaveUpdatedOrder() {
        Order order = new Order();
        order.setId(12L);
        order.setStatus(OrderStatus.PENDING);
        OrderDto expected = new OrderDto();
        expected.setId(12L);
        expected.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(expected);

        OrderDto actual = orderService.updateStatus(
                12L, new UpdateOrderStatusRequestDto(OrderStatus.DELIVERED)
        );

        assertEquals(expected, actual);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void updateStatus_ShouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.updateStatus(404L, new UpdateOrderStatusRequestDto(OrderStatus.DELIVERED))
        );

        assertEquals("Order not found: 404", exception.getMessage());
    }

    private Book createBook(Long id, BigDecimal price) {
        Book book = new Book();
        book.setId(id);
        book.setPrice(price);
        return book;
    }

    private CartItem createCartItem(Book book, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private OrderItem createOrderItemWithId(Long id, Book book, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setBook(book);
        orderItem.setQuantity(quantity);
        return orderItem;
    }
}
