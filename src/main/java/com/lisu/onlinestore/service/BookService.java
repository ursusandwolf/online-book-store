package com.lisu.onlinestore.service;

import com.lisu.onlinestore.model.Book;
import java.util.List;

public interface BookService {
    Book save(Book book);

    List<Book> findAll();
}
