package com.lisu.onlinestore;

import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookService;
import com.lisu.onlinestore.service.impl.BookServiceImpl;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private final BookService bookService = new BookServiceImpl();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Book b1 = new Book();
        b1.setTitle("Clean Code");
        b1.setAuthor("Robert C. Martin");
        b1.setIsbn("9780132350884"); // ISBN-13
        b1.setPrice(new BigDecimal("39.99"));
        b1.setDescription("A Handbook of Agile Software Craftsmanship");
        b1.setCoverImage("https://example.com/covers/clean-code.jpg");
        bookService.save(b1);

        Book b2 = new Book();
        b2.setTitle("The Mythical Man-Month");
        b2.setAuthor("Frederick P. Brooks Jr.");
        b2.setIsbn("0201835959"); // valid ISBN-10
        b2.setPrice(new BigDecimal("24.50"));
        bookService.save(b2);

        System.out.println("Saved books: " + bookService.findAll());
    }
}
