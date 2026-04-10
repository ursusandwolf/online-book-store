package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.BookRequestDto;
import com.lisu.onlinestore.model.Book;
import java.util.List;

public interface BookService {

    BookDto save(Book book);

    BookDto update(Long id, BookRequestDto dto);

    BookDto findById(Long id);

    List<BookDto> findAll();

    void delete(Long id);
}
