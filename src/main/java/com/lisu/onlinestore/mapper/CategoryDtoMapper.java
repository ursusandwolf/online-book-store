package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.CategoryResponseDto;
import com.lisu.onlinestore.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryDtoMapper {
    public CategoryResponseDto toResponseDto(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
