package com.lisu.onlinestore;

import com.lisu.onlinestore.dto.CreateBookRequestDto;
import com.lisu.onlinestore.service.BookService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class Application {

    private final BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(String... args) throws Exception {
        CreateBookRequestDto book1 = new CreateBookRequestDto();
        book1.setTitle("Clean Code");
        book1.setAuthor("Robert C. Martin");
        book1.setIsbn("9780132350884");
        book1.setPrice(new BigDecimal("39.99"));
        book1.setDescription("A Handbook of Agile Software Craftsmanship");
        book1.setCoverImage("https://example.com/covers/clean-code.jpg");
        bookService.create(book1);

        CreateBookRequestDto book2 = new CreateBookRequestDto();
        book2.setTitle("The Mythical Man-Month");
        book2.setAuthor("Frederick P. Brooks Jr.");
        book2.setIsbn("0201835959");
        book2.setPrice(new BigDecimal("24.50"));
        bookService.create(book2);

        System.out.println("Saved books: " + bookService.findAll());
    }
}
