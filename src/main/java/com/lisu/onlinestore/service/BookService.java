package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.CreateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookDto create(CreateBookRequestDto dto);

    BookDto findById(Long id);

    Page<BookDto> findAll(Pageable pageable);

    Page<BookDto> findAllByCategoriesId(Long categoryId, Pageable pageable);

    BookDto update(Long id, CreateBookRequestDto dto);

    void deleteById(Long id);
}
