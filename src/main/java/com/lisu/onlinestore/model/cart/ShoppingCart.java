package com.lisu.onlinestore.model.cart;

import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "shopping_carts")
public class ShoppingCart {
    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();

    public Optional<CartItem> findItemByBookId(Long bookId) {
        return items.stream()
                .filter(item -> item.getBook() != null && item.getBook().getId().equals(bookId))
                .findFirst();
    }

    public CartItem addItem(Book book, int quantity) {
        CartItem item = new CartItem();
        item.setBook(book);
        item.setQuantity(quantity);
        item.setShoppingCart(this);
        items.add(item);
        return item;
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setShoppingCart(null);
    }
}
