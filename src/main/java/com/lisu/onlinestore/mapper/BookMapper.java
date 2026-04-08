package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.model.Book;

public interface BookMapper {
    BookDto toDto(Book book);

    Book toModel(BookDto dto);
}
