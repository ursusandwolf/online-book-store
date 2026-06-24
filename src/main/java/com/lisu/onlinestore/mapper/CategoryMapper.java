package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import com.lisu.onlinestore.dto.category.CreateCategoryDto;
import com.lisu.onlinestore.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDto toResponseDto(Category category);

    Category toEntity(CreateCategoryDto createCategoryDto);

    void updateFromDto(CreateCategoryDto createCategoryDto, @MappingTarget Category category);

    CreateCategoryDto toDto(Category category);
}
