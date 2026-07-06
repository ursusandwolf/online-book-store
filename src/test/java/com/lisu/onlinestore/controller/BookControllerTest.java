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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class BookControllerTest extends MySqlIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void getAll_ShouldAllowUserRole() throws Exception {
        Category category = categoryRepository.save(createCategory("Software"));
        Book book = bookRepository.save(createBook(
                "Clean Code",
                "9780132350884",
                category
        ));

        mockMvc.perform(get("/books").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(book.getId()))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }

    @Test
    void getBookById_ShouldReturnNotFoundWhenBookMissing() throws Exception {
        mockMvc.perform(get("/books/99").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find book by id: 99"));
    }

    @Test
    void getBookById_ShouldReturnBook() throws Exception {
        Category category = categoryRepository.save(createCategory("Java"));
        Book book = bookRepository.save(createBook(
                "Effective Java",
                "9780134685991",
                category
        ));

        mockMvc.perform(get("/books/" + book.getId()).with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book.getId()))
                .andExpect(jsonPath("$.title").value("Effective Java"));
    }

    @Test
    void createBook_ShouldAllowAdminRole() throws Exception {
        Category category = categoryRepository.save(createCategory("Refactoring"));

        mockMvc.perform(post("/books")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(validBookPayload(category.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Refactoring"));

        Optional<Book> savedBook = bookRepository.findAll().stream()
                .filter(book -> book.getIsbn().equals("9780134757599"))
                .findFirst();
        assertTrue(savedBook.isPresent());
        assertEquals("Refactoring", savedBook.get().getTitle());
    }

    @Test
    void createBook_ShouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/books")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
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
        Category category = categoryRepository.save(createCategory("Legacy"));
        Book existingBook = bookRepository.save(createBook(
                "Original title",
                "9780201633610",
                category
        ));

        mockMvc.perform(put("/books/" + existingBook.getId())
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(validBookPayload(category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingBook.getId()))
                .andExpect(jsonPath("$.title").value("Refactoring"));

        Book updatedBook = bookRepository.findById(existingBook.getId()).orElseThrow();
        assertEquals("Refactoring", updatedBook.getTitle());
        assertEquals("9780134757599", updatedBook.getIsbn());
    }

    @Test
    void deleteBook_ShouldRejectUserRole() throws Exception {
        mockMvc.perform(delete("/books/5").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBook_ShouldAllowAdminRole() throws Exception {
        Category category = categoryRepository.save(createCategory("Business"));
        Book book = bookRepository.save(createBook(
                "The Lean Startup",
                "9780307887894",
                category
        ));

        mockMvc.perform(delete("/books/" + book.getId()).with(user(createUser(1L, RoleName.ADMIN))))
                .andExpect(status().isNoContent());

        assertFalse(bookRepository.findById(book.getId()).isPresent());
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

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Description");
        return category;
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
                  "title": "Refactoring",
                  "author": "Martin Fowler",
                  "isbn": "9780134757599",
                  "price": 49.99,
                  "description": "Refactoring book",
                  "coverImage": "cover.png",
                  "categoryIds": [%d]
                }
                """.formatted(categoryId);
    }
}
