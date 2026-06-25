package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.book.BookDto;
import com.lisu.onlinestore.dto.book.BookDtoWithoutCategoryIds;
import com.lisu.onlinestore.dto.book.CreateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookDto create(CreateBookRequestDto dto);

    BookDto findById(Long id);

    Page<BookDto> findAll(Pageable pageable);

    Page<BookDtoWithoutCategoryIds> findAllByCategoriesId(Long categoryId, Pageable pageable);

    BookDto update(Long id, CreateBookRequestDto dto);

    void deleteById(Long id);
}
