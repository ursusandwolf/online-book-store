package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.CreateBookRequestDto;
import java.util.List;

public interface BookService {

    BookDto create(CreateBookRequestDto dto);

    BookDto findById(Long id);

    List<BookDto> findAll();

    BookDto update(Long id, CreateBookRequestDto dto);

    void deleteById(Long id);
}
