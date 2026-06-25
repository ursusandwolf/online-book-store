package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dao.CartItemRepository;
import com.lisu.onlinestore.dao.ShoppingCartRepository;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final BookRepository bookRepo;

    @Transactional
    public ShoppingCart getOrCreateCart(User user) {
        return cartRepo.findWithItemsByUser(user).orElseGet(() -> {
            ShoppingCart cart = new ShoppingCart();
            cart.setUser(user);
            return cartRepo.save(cart);
        });
    }

    @Transactional
    public CartItem addItem(User user, Long bookId, int quantity) {
        ShoppingCart cart = getOrCreateCart(user);
        Book book = checkBook(bookId, quantity);
        return itemRepo.findByShoppingCartAndBook(cart, book).map(item -> {
            item.setQuantity(item.getQuantity() + quantity);
            return itemRepo.save(item);
        }).orElseGet(() -> {
            CartItem item = new CartItem();
            item.setShoppingCart(cart);
            item.setBook(book);
            item.setQuantity(quantity);
            return itemRepo.save(item);
        });
    }

    private Book checkBook(Long bookId, int quantity) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found: " + bookId));
        if (book.getStock() == null || quantity > book.getStock()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
        book.setStock(book.getStock() - quantity);
        bookRepo.save(book);
        return book;
    }

    @Transactional
    public CartItem updateQuantity(User user, Long cartItemId, int newQty) {
        CartItem item = getItem(user, cartItemId);
        Book book = item.getBook();
        int currentQty = item.getQuantity();
        int diff = newQty - currentQty;
        if (diff > 0) {
            if (book.getStock() == null || diff > book.getStock()) {
                throw new IllegalArgumentException("Requested additional quantity exceeds stock");
            }
            book.setStock(book.getStock() - diff);
            bookRepo.save(book);
        } else if (diff < 0) {
            book.setStock(book.getStock() - diff);
            bookRepo.save(book);
        }
        item.setQuantity(newQty);
        return itemRepo.save(item);
    }

    @Transactional
    public void removeItem(User user, Long cartItemId) {
        CartItem item = getItem(user, cartItemId);
        Book book = item.getBook();
        book.setStock(book.getStock() + item.getQuantity());
        bookRepo.save(book);
        itemRepo.delete(item);
    }

    private CartItem getItem(User user, Long cartItemId) {
        ShoppingCart cart = getOrCreateCart(user);
        return itemRepo.findById(cartItemId)
                .filter(i -> i.getShoppingCart().equals(cart))
                .orElseThrow(() -> new EntityNotFoundException("Item not found in your cart"));
    }

    @Transactional
    public void clearCart(User user) {
        ShoppingCart cart = getOrCreateCart(user);
        cart.getItems().forEach(item -> {
            Book book = item.getBook();
            book.setStock(book.getStock() + item.getQuantity());
            bookRepo.save(book);
        });
        itemRepo.deleteAllByShoppingCart(cart);
    }
}
