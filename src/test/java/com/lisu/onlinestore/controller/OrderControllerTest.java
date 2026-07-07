package com.lisu.onlinestore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lisu.onlinestore.Application;
import com.lisu.onlinestore.dao.OrderRepository;
import com.lisu.onlinestore.dao.ShoppingCartRepository;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import com.lisu.onlinestore.model.order.Order;
import com.lisu.onlinestore.model.order.OrderStatus;
import java.math.BigDecimal;
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
    "classpath:database/delete/delete-all-orders.sql",
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
    "classpath:database/delete/delete-all-orders.sql",
    "classpath:database/delete/delete-all-shopping-carts.sql",
    "classpath:database/delete/delete-all-users.sql",
    "classpath:database/delete/delete-books-categories-table.sql",
    "classpath:database/delete/delete-all-books.sql",
    "classpath:database/delete/delete-all-categories.sql"
}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Test
    void placeOrder_ShouldAllowUserRole() throws Exception {
        mockMvc.perform(post("/orders")
                        .with(user(createPrincipalUser(2L, RoleName.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shippingAddress": "Kyiv, Shevchenko ave, 1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        Order createdOrder = orderRepository.findAll().stream()
                .filter(order -> order.getUser().getId().equals(2L))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("134.97"), createdOrder.getTotal());
        assertEquals("Kyiv, Shevchenko ave, 1", createdOrder.getShippingAddress());

        ShoppingCart clearedCart = shoppingCartRepository.findWithItemsByUser(createDbUser(2L)).orElseThrow();
        assertTrue(clearedCart.getCartItems().isEmpty());
    }

    @Test
    @Sql(scripts = "classpath:database/create/add-default-orders.sql")
    void updateOrderStatus_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(patch("/orders/101")
                        .with(user(createPrincipalUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DELIVERED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        var updatedOrder = orderRepository.findById(101L).orElseThrow();
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
