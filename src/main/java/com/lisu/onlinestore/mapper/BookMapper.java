package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.BookRequestDto;
import com.lisu.onlinestore.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BookMapper extends BookMapperContract {
    @Override
    Book toEntity(BookRequestDto dto);

    @Override
    BookDto toDto(Book entity);

    @Override
    void updateEntityFromDto(BookRequestDto dto, @MappingTarget Book entity);

}
