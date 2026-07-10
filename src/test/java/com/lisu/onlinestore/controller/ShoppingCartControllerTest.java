package com.lisu.onlinestore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lisu.onlinestore.Application;
import com.lisu.onlinestore.dao.CartItemRepository;
import com.lisu.onlinestore.dao.ShoppingCartRepository;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = {
    "classpath:database/delete/delete-all-shopping-carts.sql",
    "classpath:database/delete/delete-all-users.sql",
    "classpath:database/delete/delete-books-categories-table.sql",
    "classpath:database/delete/delete-all-books.sql",
    "classpath:database/delete/delete-all-categories.sql",
    "classpath:database/create/add-default-users.sql",
    "classpath:database/create/add-default-categories.sql",
    "classpath:database/create/add-default-books.sql",
    "classpath:database/create/add-into-books-categories-table.sql",
    "classpath:database/create/add-default-shopping-cart.sql"
})
@Sql(scripts = {
    "classpath:database/delete/delete-all-shopping-carts.sql",
    "classpath:database/delete/delete-all-users.sql",
    "classpath:database/delete/delete-books-categories-table.sql",
    "classpath:database/delete/delete-all-books.sql",
    "classpath:database/delete/delete-all-categories.sql"
}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ShoppingCartControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void getCart_ShouldReturnCurrentUserCart() throws Exception {
        mockMvc.perform(get("/cart").with(user(createPrincipalUser(2L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.cartItems[0].bookTitle").value("Refactoring"));
    }

    @Test
    void getCart_ShouldReturnNotFoundWhenCartMissing() throws Exception {
        mockMvc.perform(get("/cart").with(user(createPrincipalUser(5L, RoleName.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Shopping cart not found for user: 5"));
    }

    @Test
    void addBook_ShouldAllowUserRole() throws Exception {
        mockMvc.perform(post("/cart")
                        .with(user(createPrincipalUser(4L, RoleName.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { 
                                  "bookId": 1,
                                  "quantity": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.quantity").value(3));

        ShoppingCart updatedCart = shoppingCartRepository.findWithItemsByUser(createDbUser(4L)).orElseThrow();
        assertEquals(1, updatedCart.getCartItems().size());
        assertTrue(updatedCart.findItemByBookId(1L).isPresent());
    }

    @Test
    void addBook_ShouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/cart")
                        .with(user(createPrincipalUser(4L, RoleName.USER)))
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
        mockMvc.perform(put("/cart/items/1")
                        .with(user(createPrincipalUser(2L, RoleName.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(5));

        var updatedItem = cartItemRepository.findById(1L).orElseThrow();
        assertEquals(5, updatedItem.getQuantity());
    }

    @Test
    void updateQuantity_ShouldReturnNotFoundForForeignCartItem() throws Exception {
        mockMvc.perform(put("/cart/items/2")
                        .with(user(createPrincipalUser(2L, RoleName.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cart item not found: 2"));
    }

    @Test
    void removeItem_ShouldAllowUserRole() throws Exception {
        mockMvc.perform(delete("/cart/items/2").with(user(createPrincipalUser(3L, RoleName.USER))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/cart").with(user(createPrincipalUser(3L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.cartItems.length()").value(0));
    }

    @Test
    void removeItem_ShouldReturnNotFoundForForeignCartItem() throws Exception {
        mockMvc.perform(delete("/cart/items/1").with(user(createPrincipalUser(3L, RoleName.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cart item not found: 1"));
    }

    @Test
    void addBook_ShouldReturnNotFoundWhenBookMissing() throws Exception {
        mockMvc.perform(post("/cart")
                        .with(user(createPrincipalUser(4L, RoleName.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 999,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Book not found: 999"));
    }

    @Test
    void getCart_ShouldRejectAdminRole() throws Exception {
        mockMvc.perform(get("/cart").with(user(createPrincipalUser(1L, RoleName.ADMIN))))
                .andExpect(status().isForbidden());
    }

    private User createDbUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user-" + id + "@test.com");
        user.setPassword("password");
        user.setRoles(Set.of());
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
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
