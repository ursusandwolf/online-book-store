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
    @Transactional(readOnly = true)
    public CartDto getCart(Long userId) {
        User user = getUserById(userId);
        ShoppingCart cart = cartRepo.findWithItemsByUser(user)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shopping cart not found for user: " + userId));
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartItemDto addBook(Long userId, AddCartItemRequest request) {
        User user = getUserById(userId);
        ShoppingCart cart = getCartByUser(user);
        Book book = getBookById(request.getBookId());

        CartItem item = itemRepo.findByShoppingCartAndBook(cart, book)
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity()
                            + request.getQuantity());
                    return itemRepo.save(existingItem);
                })
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setShoppingCart(cart);
                    newItem.setBook(book);
                    newItem.setQuantity(request.getQuantity());
                    return itemRepo.save(newItem);
                });

        return cartItemMapper.toDto(item);
    }

    @Override
    @Transactional
    public CartItemDto updateQuantity(Long userId, Long cartItemId,
                                      UpdateQuantityRequest request) {
        CartItem item = getItemByIdAndUser(userId, cartItemId);
        int newQty = request.getQuantity();

        item.setQuantity(newQty);
        CartItem updatedItem = itemRepo.save(item);
        return cartItemMapper.toDto(updatedItem);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        CartItem item = getItemByIdAndUser(userId, cartItemId);
        itemRepo.delete(item);
    }

    private ShoppingCart getCartByUser(User user) {
        return cartRepo.findWithItemsByUser(user)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shopping cart not found for user: " + user.getId()));
    }

    private CartItem getItemByIdAndUser(Long userId, Long cartItemId) {
        ShoppingCart cart = cartRepo.findWithItemsByUser(getUserById(userId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shopping cart not found for user: " + userId));

        return itemRepo.findById(cartItemId)
                .filter(item -> item.getShoppingCart().getId().equals(cart.getId()))
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
