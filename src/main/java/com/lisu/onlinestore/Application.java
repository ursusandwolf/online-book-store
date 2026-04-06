package com.lisu.onlinestore;

import java.math.BigDecimal;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class Application implements CommandLineRunner {

    private final BookService bookService;

    public Application(BookService bookService) {
        this.bookService = bookService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Book book1 = new Book();
        book1.setTitle("Clean Code");
        book1.setAuthor("Robert C. Martin");
        book1.setIsbn("9780132350884");
        book1.setPrice(new BigDecimal("39.99"));
        book1.setDescription("A Handbook of Agile Software Craftsmanship");
        book1.setCoverImage("https://example.com/covers/clean-code.jpg");
        bookService.save(book1);

        Book book2 = new Book();
        book2.setTitle("The Mythical Man-Month");
        book2.setAuthor("Frederick P. Brooks Jr.");
        book2.setIsbn("0201835959");
        book2.setPrice(new BigDecimal("24.50"));
        bookService.save(book2);

        System.out.println("Saved books: " + bookService.findAll());
    }
}
