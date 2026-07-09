package com.lisu.onlinestore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lisu.onlinestore.Application;
import com.lisu.onlinestore.dao.CategoryRepository;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
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
    "classpath:database/delete/delete-books-categories-table.sql",
    "classpath:database/delete/delete-all-books.sql",
    "classpath:database/delete/delete-all-categories.sql",
    "classpath:database/create/add-default-categories.sql",
    "classpath:database/create/add-default-books.sql",
    "classpath:database/create/add-into-books-categories-table.sql"
})
@Sql(scripts = {
    "classpath:database/delete/delete-books-categories-table.sql",
    "classpath:database/delete/delete-all-books.sql",
    "classpath:database/delete/delete-all-categories.sql"
}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void getAllCategories_ShouldAllowUserRole() throws Exception {
        mockMvc.perform(get("/categories").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Business"));
    }

    @Test
    void getById_ShouldReturnNotFoundWhenCategoryMissing() throws Exception {
        mockMvc.perform(get("/categories/55").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find category by id: 55"));
    }

    @Test
    void getById_ShouldReturnCategory() throws Exception {
        mockMvc.perform(get("/categories/1").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Software Engineering"));
    }

    @Test
    void getBooks_ShouldReturnBooksForCategory() throws Exception {
        mockMvc.perform(get("/categories/1/books")
                        .with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }

    @Test
    void create_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(post("/categories")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCategoryPayload("History")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("History"));

        Long createdCategoryId = categoryRepository.findAll().stream()
                .filter(category -> category.getName().equals("History"))
                .findFirst()
                .orElseThrow()
                .getId();
        assertEquals("History books", categoryRepository.findById(createdCategoryId).orElseThrow().getDescription());
    }

    @Test
    void update_ShouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(put("/categories/1")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(put("/categories/1")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCategoryPayload("Updated history")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated history"));

        var updatedCategory = categoryRepository.findById(1L).orElseThrow();
        assertEquals("Updated history", updatedCategory.getName());
    }

    @Test
    void delete_ShouldRejectUserRole() throws Exception {
        mockMvc.perform(delete("/categories/3").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(delete("/categories/3")
                        .with(user(createUser(1L, RoleName.ADMIN))))
                .andExpect(status().isNoContent());

        assertFalse(categoryRepository.findById(3L).isPresent());
    }

    private User createUser(Long id, RoleName roleName) {
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

    private String validCategoryPayload(String name) {
        return """
                {
                  "name": "%s",
                  "description": "History books"
                }
                """.formatted(name);
    }
}
