package com.lisu.onlinestore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lisu.onlinestore.Application;
import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dao.CartItemRepository;
import com.lisu.onlinestore.dao.CategoryRepository;
import com.lisu.onlinestore.dao.OrderRepository;
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
import com.lisu.onlinestore.model.order.Order;
import com.lisu.onlinestore.model.order.OrderStatus;
import com.lisu.onlinestore.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class OrderControllerTest extends MySqlIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void placeOrder_ShouldAllowUserRole() throws Exception {
        User currentUser = saveUser("place-order@test.com", RoleName.USER);
        Book book = saveBook("Refactoring", "9780134757599", new BigDecimal("49.99"));
        ShoppingCart cart = saveCart(currentUser);
        addItemToCart(cart, book, 3);

        mockMvc.perform(post("/orders")
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shippingAddress": "Kyiv, Shevchenko ave, 1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        Order createdOrder = orderRepository.findAll().stream()
                .filter(order -> order.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("149.97"), createdOrder.getTotal());
        assertEquals("Kyiv, Shevchenko ave, 1", createdOrder.getShippingAddress());

        ShoppingCart clearedCart = shoppingCartRepository.findWithItemsByUser(currentUser).orElseThrow();
        assertTrue(clearedCart.getCartItems().isEmpty());
    }

    @Test
    void updateOrderStatus_ShouldAllowAdminRole() throws Exception {
        User currentUser = saveUser("order-owner@test.com", RoleName.USER);
        Order order = orderRepository.save(createOrder(currentUser, OrderStatus.PENDING));

        mockMvc.perform(patch("/orders/" + order.getId())
                        .with(user(createPrincipalUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DELIVERED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
    }

    @Test
    void updateOrderStatus_ShouldRejectUserRole() throws Exception {
        mockMvc.perform(patch("/orders/101")
                        .with(user(createPrincipalUser(7L, RoleName.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DELIVERED"
                                }
                                """))
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

    private Book saveBook(String title, String isbn, BigDecimal price) {
        Category category = new Category();
        category.setName("Category-" + isbn);
        category.setDescription("Description");
        Category savedCategory = categoryRepository.save(category);

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Author");
        book.setIsbn(isbn);
        book.setPrice(price);
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

    private Order createOrder(User user, OrderStatus status) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(status);
        order.setTotal(new BigDecimal("100.00"));
        order.setOrderDate(LocalDateTime.of(2026, 1, 10, 9, 0));
        order.setShippingAddress("Kyiv, Shevchenko ave, 1");
        return order;
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
