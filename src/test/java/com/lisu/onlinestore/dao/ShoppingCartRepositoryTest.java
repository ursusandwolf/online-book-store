package com.lisu.onlinestore.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = ShoppingCartRepositoryTest.TestApplication.class)
class ShoppingCartRepositoryTest {
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByUser_ShouldReturnCart() {
        User user = userRepository.save(createUser("cart-user@test.com"));
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCartRepository.save(shoppingCart);

        ShoppingCart actual = shoppingCartRepository.findByUser(user).orElseThrow();

        assertEquals(user.getId(), actual.getId());
        assertEquals(user.getId(), actual.getUser().getId());
    }

    @Test
    void findWithItemsByUser_ShouldLoadCartItemsAndBooks() {
        User user = userRepository.save(createUser("cart-items@test.com"));
        Book book = bookRepository.save(createBook());
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(2);
        shoppingCart.addItem(cartItem);
        shoppingCartRepository.save(shoppingCart);

        ShoppingCart actual = shoppingCartRepository.findWithItemsByUser(user).orElseThrow();

        assertEquals(1, actual.getCartItems().size());
        CartItem actualItem = actual.getCartItems().iterator().next();
        assertEquals(2, actualItem.getQuantity());
        assertEquals(book.getId(), actualItem.getBook().getId());
        assertTrue(actual.findItemByBookId(book.getId()).isPresent());
    }

    private User createUser(String email) {
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(createRole(RoleName.USER)));
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles(Set.of(userRole));
        return user;
    }

    private Role createRole(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }

    private Book createBook() {
        com.lisu.onlinestore.model.Category category = new com.lisu.onlinestore.model.Category();
        category.setName("Tech");
        category.setDescription("Description");
        category = categoryRepository.save(category);

        Book book = new Book();
        book.setTitle("Domain-Driven Design");
        book.setAuthor("Eric Evans");
        book.setIsbn("9780321125217");
        book.setPrice(new BigDecimal("59.99"));
        book.setCategories(Set.of(category));
        return book;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.lisu.onlinestore.model")
    @EnableJpaRepositories("com.lisu.onlinestore.dao")
    static class TestApplication {
    }
}
