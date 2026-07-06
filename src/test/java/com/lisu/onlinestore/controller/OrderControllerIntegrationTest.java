package com.lisu.onlinestore.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lisu.onlinestore.Application;
import com.lisu.onlinestore.dao.OrderRepository;
import com.lisu.onlinestore.dao.RoleRepository;
import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class OrderControllerIntegrationTest extends MySqlIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    void getOrders_ShouldReturnCurrentUserOrders() throws Exception {
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(createRole(RoleName.USER)));

        User currentUser = userRepository.save(createUser(
                "current-user@test.com", userRole));
        User anotherUser = userRepository.save(createUser(
                "another-user@test.com", userRole));

        Order olderCurrentUserOrder = orderRepository.save(createOrder(
                currentUser,
                LocalDateTime.of(2026, 1, 10, 9, 0),
                "Kyiv, Shevchenko ave, 1"));
        Order newerCurrentUserOrder = orderRepository.save(createOrder(
                currentUser,
                LocalDateTime.of(2026, 2, 10, 9, 0),
                "Kyiv, Franka ave, 2"));
        orderRepository.save(createOrder(
                anotherUser,
                LocalDateTime.of(2026, 3, 10, 9, 0),
                "Lviv, Svobody ave, 3"));

        mockMvc.perform(get("/orders").with(user(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(newerCurrentUserOrder.getId()))
                .andExpect(jsonPath("$.content[1].id").value(olderCurrentUserOrder.getId()))
                .andExpect(jsonPath("$.content[0].userId").value(currentUser.getId()))
                .andExpect(jsonPath("$.content[1].userId").value(currentUser.getId()));
    }

    private Role createRole(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }

    private User createUser(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles(Set.of(role));
        return user;
    }

    private Order createOrder(User user, LocalDateTime orderDate, String shippingAddress) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(BigDecimal.valueOf(100));
        order.setOrderDate(orderDate);
        order.setShippingAddress(shippingAddress);
        return order;
    }
}
