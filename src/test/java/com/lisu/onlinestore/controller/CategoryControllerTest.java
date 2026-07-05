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

import com.lisu.onlinestore.dto.book.BookDtoWithoutCategoryIds;
import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import com.lisu.onlinestore.exception.CustomGlobalExceptionHandler;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.service.BookService;
import com.lisu.onlinestore.service.CategoryService;
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

@SpringBootTest(classes = CategoryControllerTest.TestApplication.class)
@AutoConfigureMockMvc
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private BookService bookService;

    @Test
    void getAllCategories_ShouldAllowUserRole() throws Exception {
        CategoryResponseDto category = createCategoryResponseDto(1L, "Fiction");
        when(categoryService.findAll(any())).thenReturn(new PageImpl<>(java.util.List.of(category)));

        mockMvc.perform(get("/categories").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Fiction"));
    }

    @Test
    void getById_ShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(categoryService.getById(55L))
                .thenThrow(new EntityNotFoundException("Can't find category by id: 55"));

        mockMvc.perform(get("/categories/55").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find category by id: 55"));
    }

    @Test
    void getById_ShouldReturnCategory() throws Exception {
        CategoryResponseDto category = createCategoryResponseDto(5L, "Science");
        when(categoryService.getById(5L)).thenReturn(category);

        mockMvc.perform(get("/categories/5").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Science"));
    }

    @Test
    void getBooks_ShouldReturnBooksForCategory() throws Exception {
        BookDtoWithoutCategoryIds book = new BookDtoWithoutCategoryIds();
        book.setId(3L);
        book.setTitle("Clean Architecture");
        when(bookService.findAllByCategoriesId(eq(4L), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(book)));

        mockMvc.perform(get("/categories/4/books").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].title").value("Clean Architecture"));
    }

    @Test
    void create_ShouldAllowAdminRole() throws Exception {
        CategoryResponseDto created = createCategoryResponseDto(2L, "History");
        when(categoryService.save(any())).thenReturn(created);

        mockMvc.perform(post("/categories")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCategoryPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("History"));

        verify(categoryService).save(any());
    }

    @Test
    void update_ShouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(put("/categories/3")
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
        CategoryResponseDto updated = createCategoryResponseDto(3L, "Updated history");
        when(categoryService.update(eq(3L), any())).thenReturn(updated);

        mockMvc.perform(put("/categories/3")
                        .with(user(createUser(1L, RoleName.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCategoryPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Updated history"));

        verify(categoryService).update(eq(3L), any());
    }

    @Test
    void delete_ShouldRejectUserRole() throws Exception {
        mockMvc.perform(delete("/categories/3").with(user(createUser(10L, RoleName.USER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(delete("/categories/3").with(user(createUser(1L, RoleName.ADMIN))))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteById(3L);
    }

    private CategoryResponseDto createCategoryResponseDto(Long id, String name) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription("Description");
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

    private String validCategoryPayload() {
        return """
                {
                  "name": "History",
                  "description": "History books"
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
    @Import({CategoryController.class, CustomGlobalExceptionHandler.class, TestSecurityConfig.class})
    static class TestApplication {
    }
}
