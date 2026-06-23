package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.book.BookDto;
import com.lisu.onlinestore.dto.book.BookDtoWithoutCategoryIds;
import com.lisu.onlinestore.dto.book.CreateBookRequestDto;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.Category;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookMapper {
    Book toBook(CreateBookRequestDto createBookRequestDto);

    BookDto toDto(Book book);

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @AfterMapping
    default void setCategoryIds(@MappingTarget BookDto bookDto, Book book) {
        if (book.getCategories() != null) {
            bookDto.setCategoryIds(
                    book.getCategories().stream()
                            .map(Category::getId)
                            .collect(Collectors.toSet()));
        }
    }
}
