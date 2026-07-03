package com.lisu.onlinestore.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lisu.onlinestore.dao.OrderRepository;
import com.lisu.onlinestore.dao.ShoppingCartRepository;
import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.order.OrderDto;
import com.lisu.onlinestore.dto.order.request.CreateOrderRequestDto;
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

        assertSame(expected, actual);
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
}
