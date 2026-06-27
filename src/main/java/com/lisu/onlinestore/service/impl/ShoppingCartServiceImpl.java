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
import com.lisu.onlinestore.mapper.ShoppingCartMapper;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import com.lisu.onlinestore.service.BookStockService;
import com.lisu.onlinestore.service.CartRequestValidator;
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
    private final BookStockService bookStockService;
    private final CartRequestValidator requestValidator;

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
        requestValidator.validateAddRequest(request);
        User user = getUserById(userId);
        ShoppingCart cart = getOrCreateCart(user);
        Book book = getBookById(request.getBookId());
        bookStockService.decreaseStock(book, request.getQuantity());

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

        return cartMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public CartItemDto updateQuantity(Long userId, Long cartItemId,
                                      UpdateQuantityRequest request) {
        requestValidator.validateUpdateRequest(request);
        CartItem item = getItemByIdAndUser(userId, cartItemId);
        Book book = item.getBook();
        int currentQty = item.getQuantity();
        int newQty = request.getQuantity();
        int diff = newQty - currentQty;

        if (diff > 0) {
            bookStockService.decreaseStock(book, diff);
        } else if (diff < 0) {
            bookStockService.increaseStock(book, -diff);
        }

        item.setQuantity(newQty);
        CartItem updatedItem = itemRepo.save(item);
        return cartMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        CartItem item = getItemByIdAndUser(userId, cartItemId);
        Book book = item.getBook();
        bookStockService.increaseStock(book, item.getQuantity());
        itemRepo.delete(item);
    }

    @Transactional
    public ShoppingCart getOrCreateCart(User user) {
        return cartRepo.findWithItemsByUser(user).orElseGet(() -> {
            ShoppingCart cart = new ShoppingCart();
            cart.setUser(user);
            return cartRepo.save(cart);
        });
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
