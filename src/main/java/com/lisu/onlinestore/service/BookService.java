package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.BookRequestDto;
import java.util.List;

public interface BookService {

    BookDto create(BookRequestDto dto);

    BookDto update(Long id, BookRequestDto dto);

    BookDto findById(Long id);

    List<BookDto> findAll();

    void delete(Long id);
}
