package com.lisu.onlinestore.dao;

import java.util.List;
import com.lisu.onlinestore.model.Book;

public interface BookRepository {
    Book save(Book book);

    Book findById(Long id);

    List<Book> findAll();
}
