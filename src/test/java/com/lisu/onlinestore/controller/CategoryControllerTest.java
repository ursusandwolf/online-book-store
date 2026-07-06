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
import com.lisu.onlinestore.dao.CategoryRepository;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.Category;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.util.Optional;
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
class CategoryControllerTest extends MySqlIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BookRepository bookRepository;

    @Test
    void getAllCategories_ShouldAllowUserRole() throws Exception {
        Category category = categoryRepository.save(createCategory("Fiction"));

        mockMvc.perform(get("/categories").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(category.getId()))
                .andExpect(jsonPath("$.content[0].name").value("Fiction"));
    }

    @Test
    void getById_ShouldReturnNotFoundWhenCategoryMissing() throws Exception {
        mockMvc.perform(get("/categories/55").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find category by id: 55"));
    }

    @Test
    void getById_ShouldReturnCategory() throws Exception {
        Category category = categoryRepository.save(createCategory("Science"));

        mockMvc.perform(get("/categories/" + category.getId()).with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Science"));
    }

    @Test
    void getBooks_ShouldReturnBooksForCategory() throws Exception {
        Category matchingCategory = categoryRepository.save(createCategory("Architecture"));
        Category otherCategory = categoryRepository.save(createCategory("Other"));
        Book matchingBook = bookRepository.save(createBook(
                "Clean Architecture",
                "9780134494166",
                matchingCategory
        ));
        bookRepository.save(createBook(
                "Domain-Driven Design",
                "9780321125217",
                otherCategory
        ));

        mockMvc.perform(get("/categories/" + matchingCategory.getId() + "/books")
                        .with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(matchingBook.getId()))
                .andExpect(jsonPath("$.content[0].title").value("Clean Architecture"));
    }

    @Test
    void create_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(post("/categories")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCategoryPayload("History")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("History"));

        Optional<Category> savedCategory = categoryRepository.findAll().stream()
                .filter(category -> category.getName().equals("History"))
                .findFirst();
        assertTrue(savedCategory.isPresent());
        assertEquals("History books", savedCategory.get().getDescription());
    }

    @Test
    void update_ShouldReturnBadRequestForInvalidPayload() throws Exception {
        Category category = categoryRepository.save(createCategory("Old"));

        mockMvc.perform(put("/categories/" + category.getId())
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
        Category category = categoryRepository.save(createCategory("History"));

        mockMvc.perform(put("/categories/" + category.getId())
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCategoryPayload("Updated history")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Updated history"));

        Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow();
        assertEquals("Updated history", updatedCategory.getName());
    }

    @Test
    void delete_ShouldRejectUserRole() throws Exception {
        mockMvc.perform(delete("/categories/3").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_ShouldAllowAdminRole() throws Exception {
        Category category = categoryRepository.save(createCategory("Business"));

        mockMvc.perform(delete("/categories/" + category.getId())
                        .with(user(createUser(1L, RoleName.ADMIN))))
                .andExpect(status().isNoContent());

        assertFalse(categoryRepository.findById(category.getId()).isPresent());
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Description");
        return category;
    }

    private Book createBook(String title, String isbn, Category category) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Author");
        book.setIsbn(isbn);
        book.setPrice(new BigDecimal("29.99"));
        book.setDescription("Description");
        book.setCoverImage("cover.png");
        book.setCategories(Set.of(category));
        return book;
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
