package com.lisu.onlinestore.dao;

import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByIdAndShoppingCartId(Long id, Long shoppingCartId);

    Optional<CartItem> findByShoppingCartAndBook(ShoppingCart cart, Book book);

    void deleteAllByShoppingCart(ShoppingCart cart);
}
