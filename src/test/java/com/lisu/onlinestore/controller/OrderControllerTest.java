package com.lisu.onlinestore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lisu.onlinestore.dto.order.OrderDto;
import com.lisu.onlinestore.dto.order.request.UpdateOrderStatusRequestDto;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.model.order.OrderStatus;
import com.lisu.onlinestore.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = OrderControllerTest.TestApplication.class)
@AutoConfigureMockMvc
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private OrderService orderService;

    @Test
    void getOrders_ShouldReturnCurrentUserOrders() throws Exception {
        User currentUser = createUser(7L, RoleName.USER);
        OrderDto order = new OrderDto();
        order.setId(101L);

        when(orderService.getOrders(eq(currentUser.getId()), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(order)));

        mockMvc.perform(get("/orders").with(user(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(101));

        verify(orderService).getOrders(eq(currentUser.getId()), any());
    }

    @Test
    void placeOrder_ShouldAllowUserRole() throws Exception {
        User currentUser = createUser(7L, RoleName.USER);
        OrderDto order = new OrderDto();
        order.setId(101L);
        order.setStatus(OrderStatus.PENDING);

        when(orderService.placeOrder(eq(currentUser.getId()), any())).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shippingAddress": "Kyiv, Shevchenko ave, 1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).placeOrder(eq(currentUser.getId()), any());
    }

    @Test
    void updateOrderStatus_ShouldAllowAdminRole() throws Exception {
        User admin = createUser(1L, RoleName.ADMIN);
        OrderDto order = new OrderDto();
        order.setId(101L);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderService.updateStatus(eq(101L), any(UpdateOrderStatusRequestDto.class)))
                .thenReturn(order);

        mockMvc.perform(patch("/orders/101")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DELIVERED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        verify(orderService).updateStatus(eq(101L), any(UpdateOrderStatusRequestDto.class));
    }

    @Test
    void updateOrderStatus_ShouldRejectUserRole() throws Exception {
        User currentUser = createUser(7L, RoleName.USER);

        mockMvc.perform(patch("/orders/101")
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DELIVERED"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private User createUser(Long id, RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);

        User user = new User();
        user.setId(id);
        user.setEmail(roleName.name().toLowerCase() + "@test.com");
        user.setPassword("password");
        user.setRoles(java.util.Set.of(role));
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            LiquibaseAutoConfiguration.class
    })
    @Import({OrderController.class, TestSecurityConfig.class})
    static class TestApplication {
    }
}
