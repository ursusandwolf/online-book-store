package com.lisu.onlinestore.dao;

import com.lisu.onlinestore.model.Book;
import java.util.List;

public interface BookRepository {
    Book save(Book book);

    List<Book> findAll();
}
