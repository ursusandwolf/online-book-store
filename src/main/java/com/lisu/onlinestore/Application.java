package com.lisu.onlinestore;

import com.lisu.onlinestore.config.AppConfig;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        //SpringApplication.run(Application.class, args);
        AnnotationConfigApplicationContext context
                = new AnnotationConfigApplicationContext(AppConfig.class);
        BookService service = context.getBean(BookService.class);

        service.save(new Book("Master"));
        service.findAll();
    }
}
