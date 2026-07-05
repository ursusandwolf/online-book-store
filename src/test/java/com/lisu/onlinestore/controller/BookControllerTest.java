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

import com.lisu.onlinestore.dto.book.BookDto;
import com.lisu.onlinestore.exception.CustomGlobalExceptionHandler;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.service.BookService;
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

@SpringBootTest(classes = BookControllerTest.TestApplication.class)
@AutoConfigureMockMvc
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookService bookService;

    @Test
    void getAll_ShouldAllowUserRole() throws Exception {
        BookDto book = createBookDto(1L, "Clean Code");
        when(bookService.findAll(any())).thenReturn(new PageImpl<>(java.util.List.of(book)));

        mockMvc.perform(get("/books").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }

    @Test
    void getBookById_ShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(bookService.findById(99L)).thenThrow(new EntityNotFoundException("Can't find book by id: 99"));

        mockMvc.perform(get("/books/99").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find book by id: 99"));
    }

    @Test
    void getBookById_ShouldReturnBook() throws Exception {
        BookDto book = createBookDto(2L, "Effective Java");
        when(bookService.findById(2L)).thenReturn(book);

        mockMvc.perform(get("/books/2").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Effective Java"));
    }

    @Test
    void createBook_ShouldAllowAdminRole() throws Exception {
        BookDto createdBook = createBookDto(5L, "Refactoring");
        when(bookService.create(any())).thenReturn(createdBook);

        mockMvc.perform(post("/books")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Refactoring"));

        verify(bookService).create(any());
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
        BookDto updatedBook = createBookDto(7L, "Updated title");
        when(bookService.update(eq(7L), any())).thenReturn(updatedBook);

        mockMvc.perform(put("/books/7")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("Updated title"));

        verify(bookService).update(eq(7L), any());
    }

    @Test
    void deleteBook_ShouldRejectUserRole() throws Exception {
        mockMvc.perform(delete("/books/5").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBook_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(delete("/books/5").with(user(createUser(1L, RoleName.ADMIN))))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(5L);
    }

    private BookDto createBookDto(Long id, String title) {
        BookDto dto = new BookDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setAuthor("Author");
        return dto;
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

    private String validBookPayload() {
        return """
                {
                  "title": "Refactoring",
                  "author": "Martin Fowler",
                  "isbn": "9780134757599",
                  "price": 49.99,
                  "description": "Refactoring book",
                  "coverImage": "cover.png",
                  "categoryIds": [1]
                }
                """;
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
    @Import({BookController.class, CustomGlobalExceptionHandler.class, TestSecurityConfig.class})
    static class TestApplication {
    }
}
