package com.lisu.onlinestore.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.lisu.onlinestore.support.MySqlIntegrationTest;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.Category;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = BookRepositoryTest.TestApplication.class)
class BookRepositoryTest extends MySqlIntegrationTest {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findAllByCategoriesId_ShouldReturnOnlyBooksFromRequestedCategory() {
        Category categoryOne = categoryRepository.save(createCategory("Fiction"));
        Category categoryTwo = categoryRepository.save(createCategory("Science"));

        Book matchingBook = bookRepository.save(createBook(
                "Clean Code",
                "9780132350884",
                Set.of(categoryOne, categoryTwo)
        ));
        bookRepository.save(createBook(
                "Deep Work",
                "9781455586691",
                Set.of(categoryTwo)
        ));

        Page<Book> books = bookRepository.findAllByCategoriesId(categoryOne.getId(), PageRequest.of(0, 10));

        assertEquals(1, books.getTotalElements());
        assertEquals(matchingBook.getId(), books.getContent().get(0).getId());
    }

    @Test
    void deleteById_ShouldUseSoftDelete() {
        Category category = categoryRepository.save(createCategory("Business"));
        Book book = bookRepository.save(createBook(
                "The Lean Startup",
                "9780307887894",
                Set.of(category)
        ));

        bookRepository.deleteById(book.getId());
        bookRepository.flush();

        assertFalse(bookRepository.findById(book.getId()).isPresent());
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Description");
        return category;
    }

    private Book createBook(String title, String isbn, Set<Category> categories) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Author");
        book.setIsbn(isbn);
        book.setPrice(new BigDecimal("29.99"));
        book.setCategories(categories);
        return book;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.lisu.onlinestore.model")
    @EnableJpaRepositories("com.lisu.onlinestore.dao")
    static class TestApplication {
    }
}
