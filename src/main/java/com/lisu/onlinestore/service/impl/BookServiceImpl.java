package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository repository;

    @Override
    public Book save(Book book) {
        return repository.save(book);
    }

    @Override
    public List<Book> findAll() {
        return repository.findAll();
    }
}
