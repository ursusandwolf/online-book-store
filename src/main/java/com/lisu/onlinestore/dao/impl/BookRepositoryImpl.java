package com.lisu.onlinestore.dao.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.model.Book;
import java.util.List;

//@Dao
public class BookRepositoryImpl implements BookRepository {
    @Override
    public Book save(Book book) {
        return null;
    }

    @Override
    public List<Book> findAll() {
        return List.of();
    }
}
