package com.lisu.onlinestore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lisu.onlinestore.Application;
import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dao.CartItemRepository;
import com.lisu.onlinestore.dao.CategoryRepository;
import com.lisu.onlinestore.dao.RoleRepository;
import com.lisu.onlinestore.dao.ShoppingCartRepository;
import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.Category;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import com.lisu.onlinestore.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class ShoppingCartControllerTest extends MySqlIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void getCart_ShouldReturnCurrentUserCart() throws Exception {
        User currentUser = saveUser("cart-user@test.com", RoleName.USER);
        Book book = saveBook("Clean Code", "9780132350884");
        ShoppingCart cart = saveCart(currentUser);
        addItemToCart(cart, book, 2);

        mockMvc.perform(get("/cart").with(user(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cart.getId()))
                .andExpect(jsonPath("$.userId").value(currentUser.getId()))
                .andExpect(jsonPath("$.cartItems[0].bookTitle").value("Clean Code"));
    }

    @Test
    void getCart_ShouldReturnNotFoundWhenCartMissing() throws Exception {
        User currentUser = saveUser("without-cart@test.com", RoleName.USER);

        mockMvc.perform(get("/cart").with(user(currentUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Shopping cart not found for user: " + currentUser.getId()));
    }

    @Test
    void addBook_ShouldAllowUserRole() throws Exception {
        User currentUser = saveUser("add-book@test.com", RoleName.USER);
        ShoppingCart cart = saveCart(currentUser);
        Book book = saveBook("Refactoring", "9780134757599");

        mockMvc.perform(post("/cart")
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "quantity": 3
                                }
                                """.formatted(book.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(book.getId()))
                .andExpect(jsonPath("$.quantity").value(3));

        ShoppingCart updatedCart = shoppingCartRepository.findWithItemsByUser(currentUser).orElseThrow();
        assertEquals(1, updatedCart.getCartItems().size());
        assertTrue(updatedCart.findItemByBookId(book.getId()).isPresent());
    }

    @Test
    void addBook_ShouldRejectInvalidPayload() throws Exception {
        User currentUser = saveUser("invalid-cart@test.com", RoleName.USER);
        saveCart(currentUser);

        mockMvc.perform(post("/cart")
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": null,
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void updateQuantity_ShouldAllowUserRole() throws Exception {
        User currentUser = saveUser("update-quantity@test.com", RoleName.USER);
        Book book = saveBook("Book", "9780321127426");
        ShoppingCart cart = saveCart(currentUser);
        CartItem item = addItemToCart(cart, book, 2);

        mockMvc.perform(put("/cart/items/" + item.getId())
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.quantity").value(5));

        CartItem updatedItem = cartItemRepository.findById(item.getId()).orElseThrow();
        assertEquals(5, updatedItem.getQuantity());
    }

    @Test
    void removeItem_ShouldAllowUserRole() throws Exception {
        User currentUser = saveUser("remove-item@test.com", RoleName.USER);
        Book book = saveBook("The Pragmatic Programmer", "9780201616224");
        ShoppingCart cart = saveCart(currentUser);
        CartItem item = addItemToCart(cart, book, 1);

        mockMvc.perform(delete("/cart/items/" + item.getId()).with(user(currentUser)))
                .andExpect(status().isNoContent());

        assertFalse(cartItemRepository.findById(item.getId()).isPresent());
    }

    @Test
    void getCart_ShouldRejectAdminRole() throws Exception {
        mockMvc.perform(get("/cart").with(user(createPrincipalUser(1L, RoleName.ADMIN))))
                .andExpect(status().isForbidden());
    }

    private User saveUser(String email, RoleName roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(createRole(roleName)));

        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setRoles(Set.of(role));
        user.setFirstName("Test");
        user.setLastName("User");
        return userRepository.save(user);
    }

    private Role createRole(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }

    private ShoppingCart saveCart(User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        return shoppingCartRepository.save(shoppingCart);
    }

    private Book saveBook(String title, String isbn) {
        Category category = new Category();
        category.setName("Category-" + isbn);
        category.setDescription("Description");
        Category savedCategory = categoryRepository.save(category);

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Author");
        book.setIsbn(isbn);
        book.setPrice(new BigDecimal("29.99"));
        book.setDescription("Description");
        book.setCoverImage("cover.png");
        book.setCategories(Set.of(savedCategory));
        return bookRepository.save(book);
    }

    private CartItem createCartItem(ShoppingCart cart, Book book, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setShoppingCart(cart);
        cartItem.setBook(book);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private CartItem addItemToCart(ShoppingCart cart, Book book, int quantity) {
        CartItem cartItem = createCartItem(cart, book, quantity);
        cart.addItem(cartItem);
        ShoppingCart savedCart = shoppingCartRepository.save(cart);
        return savedCart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(book.getId()))
                .findFirst()
                .orElseThrow();
    }

    private User createPrincipalUser(Long id, RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);

        User user = new User();
        user.setId(id);
        user.setEmail(roleName.name().toLowerCase() + "@test.com");
        user.setPassword("password");
        user.setRoles(Set.of(role));
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }
}
