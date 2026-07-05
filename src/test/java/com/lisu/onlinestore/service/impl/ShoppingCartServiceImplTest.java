package com.lisu.onlinestore.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dao.CartItemRepository;
import com.lisu.onlinestore.dao.ShoppingCartRepository;
import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.dto.cart.CartItemDto;
import com.lisu.onlinestore.dto.cart.request.CartItemRequestDto;
import com.lisu.onlinestore.dto.cart.request.UpdateQuantityRequestDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.CartItemMapper;
import com.lisu.onlinestore.mapper.ShoppingCartMapper;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {
    @Mock
    private ShoppingCartRepository cartRepo;
    @Mock
    private CartItemRepository itemRepo;
    @Mock
    private BookRepository bookRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private ShoppingCartMapper cartMapper;
    @Mock
    private CartItemMapper itemMapper;
    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    private User user;
    private ShoppingCart shoppingCart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");

        shoppingCart = new ShoppingCart();
        shoppingCart.setId(user.getId());
        shoppingCart.setUser(user);
    }

    @Test
    void createCartForUser_ShouldSaveCart() {
        shoppingCartService.createCartForUser(user);

        verify(cartRepo).save(any(ShoppingCart.class));
    }

    @Test
    void getCart_ShouldReturnMappedDto() {
        CartDto expected = new CartDto();
        expected.setId(1L);
        expected.setUserId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepo.findWithItemsByUser(user)).thenReturn(Optional.of(shoppingCart));
        when(cartMapper.toDto(shoppingCart)).thenReturn(expected);

        CartDto actual = shoppingCartService.getCart(1L);

        assertSame(expected, actual);
    }

    @Test
    void addBook_ShouldCreateNewCartItemWhenBookAbsent() {
        CartItemRequestDto request = new CartItemRequestDto(2L, 3);
        Book book = createBook(2L, "Clean Code");
        CartItem mappedItem = new CartItem();
        mappedItem.setQuantity(3);
        CartItemDto expected = new CartItemDto(10L, 2L, "Clean Code", 3);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepo.findWithItemsByUser(user)).thenReturn(Optional.of(shoppingCart));
        when(bookRepo.findById(2L)).thenReturn(Optional.of(book));
        when(itemMapper.toCartItem(request)).thenReturn(mappedItem);
        when(itemMapper.toDto(mappedItem)).thenReturn(expected);

        CartItemDto actual = shoppingCartService.addBook(1L, request);

        assertSame(expected, actual);
        assertEquals(shoppingCart, mappedItem.getShoppingCart());
        assertEquals(book, mappedItem.getBook());
        verify(cartRepo).save(shoppingCart);
    }

    @Test
    void addBook_ShouldIncreaseQuantityWhenItemAlreadyExists() {
        CartItemRequestDto request = new CartItemRequestDto(2L, 4);
        Book book = createBook(2L, "Refactoring");
        CartItem existingItem = new CartItem();
        existingItem.setId(11L);
        existingItem.setBook(book);
        existingItem.setQuantity(2);
        shoppingCart.addItem(existingItem);
        CartItemDto expected = new CartItemDto(11L, 2L, "Refactoring", 6);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepo.findWithItemsByUser(user)).thenReturn(Optional.of(shoppingCart));
        when(bookRepo.findById(2L)).thenReturn(Optional.of(book));
        when(itemMapper.toDto(existingItem)).thenReturn(expected);

        CartItemDto actual = shoppingCartService.addBook(1L, request);

        assertSame(expected, actual);
        assertEquals(6, existingItem.getQuantity());
        verify(itemMapper, never()).toCartItem(request);
    }

    @Test
    void updateQuantity_ShouldSaveUpdatedItem() {
        CartItem item = new CartItem();
        item.setId(12L);
        item.setShoppingCart(shoppingCart);
        item.setQuantity(1);
        CartItemDto expected = new CartItemDto(12L, 2L, "Book", 5);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepo.findWithItemsByUser(user)).thenReturn(Optional.of(shoppingCart));
        when(itemRepo.findByIdAndShoppingCartId(12L, shoppingCart.getId()))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(expected);

        CartItemDto actual = shoppingCartService.updateQuantity(
                1L, 12L, new UpdateQuantityRequestDto(5)
        );

        assertSame(expected, actual);
        assertEquals(5, item.getQuantity());
    }

    @Test
    void removeItem_ShouldDeleteItemFromRepository() {
        CartItem item = new CartItem();
        item.setId(15L);
        item.setShoppingCart(shoppingCart);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepo.findWithItemsByUser(user)).thenReturn(Optional.of(shoppingCart));
        when(itemRepo.findByIdAndShoppingCartId(15L, shoppingCart.getId()))
                .thenReturn(Optional.of(item));

        shoppingCartService.removeItem(1L, 15L);

        verify(itemRepo).delete(item);
    }

    @Test
    void getCart_ShouldThrowWhenUserDoesNotExist() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> shoppingCartService.getCart(99L)
        );

        assertEquals("User not found: 99", exception.getMessage());
    }

    @Test
    void addBook_ShouldThrowWhenBookDoesNotExist() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepo.findWithItemsByUser(user)).thenReturn(Optional.of(shoppingCart));
        when(bookRepo.findById(77L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> shoppingCartService.addBook(1L, new CartItemRequestDto(77L, 1))
        );

        assertEquals("Book not found: 77", exception.getMessage());
    }

    private Book createBook(Long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        return book;
    }
}
