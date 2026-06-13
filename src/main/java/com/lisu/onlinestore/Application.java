package com.lisu.onlinestore;

import com.lisu.onlinestore.service.BookService;
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
}
