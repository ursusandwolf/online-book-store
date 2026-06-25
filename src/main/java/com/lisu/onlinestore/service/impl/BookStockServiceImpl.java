package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookStockServiceImpl implements BookStockService {

    private final BookRepository bookRepo;

    @Override
    public void validateStockAvailable(Book book, int requestedQuantity) {
        if (book.getStock() == null || requestedQuantity > book.getStock()) {
            throw new IllegalArgumentException(
                    "Requested quantity exceeds available stock");
        }
    }

    @Override
    @Transactional
    public void decreaseStock(Book book, int quantity) {
        book.setStock(book.getStock() - quantity);
        bookRepo.save(book);
    }

    @Override
    @Transactional
    public void increaseStock(Book book, int quantity) {
        book.setStock(book.getStock() + quantity);
        bookRepo.save(book);
    }
}
