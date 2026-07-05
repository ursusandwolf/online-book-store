package com.lisu.onlinestore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.dto.cart.CartItemDto;
import com.lisu.onlinestore.exception.CustomGlobalExceptionHandler;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.service.ShoppingCartService;
import java.util.List;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ShoppingCartControllerTest.TestApplication.class)
@AutoConfigureMockMvc
class ShoppingCartControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ShoppingCartService shoppingCartService;

    @Test
    void getCart_ShouldReturnCurrentUserCart() throws Exception {
        CartDto cartDto = new CartDto(1L, 7L, List.of(new CartItemDto(3L, 2L, "Clean Code", 2)));
        when(shoppingCartService.getCart(7L)).thenReturn(cartDto);

        mockMvc.perform(get("/cart").with(user(createUser(7L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.cartItems[0].bookTitle").value("Clean Code"));
    }

    @Test
    void getCart_ShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(shoppingCartService.getCart(7L))
                .thenThrow(new EntityNotFoundException("Shopping cart not found for user: 7"));

        mockMvc.perform(get("/cart").with(user(createUser(7L, RoleName.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Shopping cart not found for user: 7"));
    }

    @Test
    void addBook_ShouldAllowUserRole() throws Exception {
        CartItemDto itemDto = new CartItemDto(4L, 2L, "Refactoring", 3);
        when(shoppingCartService.addBook(eq(7L), any())).thenReturn(itemDto);

        mockMvc.perform(post("/cart")
                        .with(user(createUser(7L, RoleName.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 2,
                                  "quantity": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.quantity").value(3));

        verify(shoppingCartService).addBook(eq(7L), any());
    }

    @Test
    void addBook_ShouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/cart")
                        .with(user(createUser(7L, RoleName.USER)))
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
        CartItemDto itemDto = new CartItemDto(5L, 2L, "Book", 5);
        when(shoppingCartService.updateQuantity(eq(7L), eq(5L), any())).thenReturn(itemDto);

        mockMvc.perform(put("/cart/items/5")
                        .with(user(createUser(7L, RoleName.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.quantity").value(5));

        verify(shoppingCartService).updateQuantity(eq(7L), eq(5L), any());
    }

    @Test
    void removeItem_ShouldAllowUserRole() throws Exception {
        mockMvc.perform(delete("/cart/items/5").with(user(createUser(7L, RoleName.USER))))
                .andExpect(status().isNoContent());

        verify(shoppingCartService).removeItem(7L, 5L);
    }

    @Test
    void getCart_ShouldRejectAdminRole() throws Exception {
        mockMvc.perform(get("/cart").with(user(createUser(1L, RoleName.ADMIN))))
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
    @Import({ShoppingCartController.class, CustomGlobalExceptionHandler.class, TestSecurityConfig.class})
    static class TestApplication {
    }
}
