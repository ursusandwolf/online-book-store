package com.lisu.onlinestore.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lisu.onlinestore.Application;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = {
    "classpath:database/delete/delete-all-orders.sql",
    "classpath:database/delete/delete-all-shopping-carts.sql",
    "classpath:database/delete/delete-all-users.sql",
    "classpath:database/create/add-default-users.sql",
    "classpath:database/create/add-default-orders.sql"
})
@Sql(scripts = {
    "classpath:database/delete/delete-all-orders.sql",
    "classpath:database/delete/delete-all-shopping-carts.sql",
    "classpath:database/delete/delete-all-users.sql"
}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class OrderControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getOrders_ShouldReturnCurrentUserOrders() throws Exception {
        mockMvc.perform(get("/orders").with(user(createUser(2L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(102))
                .andExpect(jsonPath("$.content[1].id").value(101))
                .andExpect(jsonPath("$.content[0].userId").value(2))
                .andExpect(jsonPath("$.content[1].userId").value(2));
    }

    private User createUser(Long id, RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);

        User user = new User();
        user.setId(id);
        user.setEmail(roleName.name().toLowerCase() + id + "@test.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles(Set.of(role));
        return user;
    }
}
