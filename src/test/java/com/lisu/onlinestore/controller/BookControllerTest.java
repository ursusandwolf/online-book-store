package com.lisu.onlinestore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = {
    "classpath:database/create/add-default-categories.sql",
    "classpath:database/create/add-default-books.sql",
    "classpath:database/create/add-into-books-categories-table.sql"
})
@Sql(scripts = {
    "classpath:database/delete/delete-books-categories-table.sql",
    "classpath:database/delete/delete-all-books.sql",
    "classpath:database/delete/delete-all-categories.sql"
}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAll_ShouldAllowUserRole() throws Exception {
        mockMvc.perform(get("/books").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }

    @Test
    void getBookById_ShouldReturnNotFoundWhenBookMissing() throws Exception {
        mockMvc.perform(get("/books/999").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find book by id: 999"));
    }

    @Test
    void getBookById_ShouldReturnBook() throws Exception {
        mockMvc.perform(get("/books/1").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"));
    }

    @Test
    void createBook_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(post("/books")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookPayload(1L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("9780000000000"));
    }

    @Test
    void createBook_ShouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/books")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "author": "",
                                  "isbn": "invalid",
                                  "price": 0,
                                  "categoryIds": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void updateBook_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(put("/books/1")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookPayloadWithIsbn(1L, "9781111111111")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Book"));
    }

    @Test
    void deleteBook_ShouldRejectUserRole() throws Exception {
        mockMvc.perform(delete("/books/1").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBook_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(delete("/books/1").with(user(createUser(1L, RoleName.ADMIN))))
                .andExpect(status().isNoContent());
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

    private String validBookPayload(Long categoryId) {
        return """
                {
                  "title": "Test Book",
                  "author": "Test Author",
                  "isbn": "9780000000000",
                  "price": 29.99,
                  "description": "Test description",
                  "coverImage": "test-cover.jpg",
                  "categoryIds": [%d]
                }
                """.formatted(categoryId);
    }

    private String validBookPayloadWithIsbn(Long categoryId, String isbn) {
        return """
                {
                  "title": "Updated Book",
                  "author": "Updated Author",
                  "isbn": "%s",
                  "price": 39.99,
                  "description": "Updated description",
                  "coverImage": "updated-cover.jpg",
                  "categoryIds": [%d]
                }
                """.formatted(isbn, categoryId);
    }
}
