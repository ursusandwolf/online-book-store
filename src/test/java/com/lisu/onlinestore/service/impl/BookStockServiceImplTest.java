package com.lisu.onlinestore.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.exception.OptimisticLockConflictException;
import com.lisu.onlinestore.model.Book;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookStockServiceImplTest {
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookStockServiceImpl bookStockService;

    @Test
    void decreaseStockShouldReserveStockWhenVersionMatches() {
        Book book = book(1L, 3, 5);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.reserveStock(1L, 3, 2)).thenReturn(1);

        assertDoesNotThrow(() -> bookStockService.decreaseStock(book, 2));

        verify(bookRepository).reserveStock(1L, 3, 2);
    }

    @Test
    void decreaseStockShouldThrowWhenStockIsInsufficient() {
        Book staleBook = book(1L, 2, 5);
        Book currentBook = book(1L, 3, 1);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(currentBook));
        when(bookRepository.reserveStock(1L, 3, 2)).thenReturn(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookStockService.decreaseStock(staleBook, 2)
        );

        assertEquals("Requested quantity exceeds available stock", exception.getMessage());
    }

    @Test
    void decreaseStockShouldFailAfterRetryLimit() {
        Book book = book(1L, 3, 10);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.reserveStock(eq(1L), anyInt(), eq(2))).thenReturn(0);

        assertThrows(
                OptimisticLockConflictException.class,
                () -> bookStockService.decreaseStock(book, 2)
        );

        verify(bookRepository, times(3)).findById(1L);
    }

    private Book book(Long id, int version, int stock) {
        Book book = new Book();
        book.setId(id);
        book.setVersion(version);
        book.setStock(stock);
        return book;
    }
}
