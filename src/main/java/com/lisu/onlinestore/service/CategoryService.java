package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.category.CategoryDto;
import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import java.util.List;

public interface CategoryService {
    CategoryResponseDto getById(Long id);

    List<CategoryResponseDto> findAll();

    CategoryResponseDto save(CategoryDto categoryDto);

    void deleteById(Long id);

    CategoryResponseDto update(Long id, CategoryDto categoryDto);
}
