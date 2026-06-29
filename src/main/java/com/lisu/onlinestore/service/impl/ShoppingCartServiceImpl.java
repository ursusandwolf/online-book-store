package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dao.CartItemRepository;
import com.lisu.onlinestore.dao.ShoppingCartRepository;
import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.dto.cart.CartItemDto;
import com.lisu.onlinestore.dto.cart.request.AddCartItemRequest;
import com.lisu.onlinestore.dto.cart.request.UpdateQuantityRequest;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.CartItemMapper;
import com.lisu.onlinestore.mapper.ShoppingCartMapper;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import com.lisu.onlinestore.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final ShoppingCartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional
    public void createCartForUser(User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        cartRepo.save(shoppingCart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(Long userId) {
        return cartMapper.toDto(getCartByUserId(userId));
    }

    @Override
    @Transactional
    public CartItemDto addBook(Long userId, AddCartItemRequest request) {
        ShoppingCart cart = getCartByUserId(userId);
        Book book = getBookById(request.getBookId());

        CartItem item = cart.findItemByBookId(book.getId())
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity()
                            + request.getQuantity());
                    return existingItem;
                })
                .orElseGet(() -> cart.addItem(book, request.getQuantity()));

        cartRepo.save(cart);

        return cartItemMapper.toDto(item);
    }

    @Override
    @Transactional
    public CartItemDto updateQuantity(Long userId, Long cartItemId,
                                      UpdateQuantityRequest request) {
        CartItem item = getItemByIdAndUserId(userId, cartItemId);

        item.setQuantity(request.getQuantity());
        CartItem updatedItem = itemRepo.save(item);
        return cartItemMapper.toDto(updatedItem);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        itemRepo.delete(getItemByIdAndUserId(userId, cartItemId));
    }

    private ShoppingCart getCartByUser(User user) {
        return cartRepo.findWithItemsByUser(user)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shopping cart not found for user: " + user.getId()));
    }

    private ShoppingCart getCartByUserId(Long userId) {
        return getCartByUser(getUserById(userId));
    }

    private CartItem getItemByIdAndUserId(Long userId, Long cartItemId) {
        ShoppingCart cart = getCartByUserId(userId);
        return itemRepo.findByIdAndShoppingCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cart item not found: " + cartItemId));
    }

    private User getUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private Book getBookById(Long bookId) {
        return bookRepo.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found: " + bookId));
    }
}
