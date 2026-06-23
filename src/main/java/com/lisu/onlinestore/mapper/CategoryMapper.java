package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.category.CategoryDto;
import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import com.lisu.onlinestore.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    // For converting entity to response DTO
    CategoryResponseDto toResponseDto(Category category);
    
    // For converting request DTO to entity
    Category toEntity(CategoryDto categoryDto);
    
    // (Optional) Convert entity to simpler DTO if needed
    CategoryDto toDto(Category category);
}