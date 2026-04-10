package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.BookRequestDto;
import com.lisu.onlinestore.model.Book;

public interface BookMapperContract {

    Book toEntity(BookRequestDto dto);

    BookDto toDto(Book entity);

    /**
     * Update existed entity from DTO's values.
     * Null fields in dto won't be affected.
     */
    void updateEntityFromDto(BookRequestDto dto, Book entity);
}
