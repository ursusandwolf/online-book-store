package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.category.CategoryDto;
import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> findAll();

    CategoryResponseDto getById(Long id);

    void deleteById(Long id);

    CategoryResponseDto save(CategoryDto categoryDto);

    CategoryResponseDto update(Long id, CategoryDto categoryDto);
}
