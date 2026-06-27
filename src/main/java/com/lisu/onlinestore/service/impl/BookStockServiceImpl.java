package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.exception.OptimisticLockConflictException;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookStockServiceImpl implements BookStockService {
    private static final int MAX_RETRIES = 3;

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
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            Book currentBook = getCurrentBook(book.getId());
            int updatedRows = bookRepo.reserveStock(
                    currentBook.getId(),
                    currentBook.getVersion(),
                    quantity
            );
            if (updatedRows > 0) {
                return;
            }
            if (currentBook.getStock() < quantity) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock");
            }
        }
        throw new OptimisticLockConflictException(
                "Book stock was updated concurrently. Retry the cart operation."
        );
    }

    @Override
    @Transactional
    public void increaseStock(Book book, int quantity) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            Book currentBook = getCurrentBook(book.getId());
            int updatedRows = bookRepo.releaseStock(
                    currentBook.getId(),
                    currentBook.getVersion(),
                    quantity
            );
            if (updatedRows > 0) {
                return;
            }
        }
        throw new OptimisticLockConflictException(
                "Book stock was updated concurrently. Retry the cart operation."
        );
    }

    private Book getCurrentBook(Long bookId) {
        return bookRepo.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found: " + bookId));
    }
}
