package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.category.CategoryDto;
import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import com.lisu.onlinestore.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDto toResponseDto(Category category);

    Category toEntity(CategoryDto categoryDto);

    CategoryDto toDto(Category category);
}
