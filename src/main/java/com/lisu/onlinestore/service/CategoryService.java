package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import com.lisu.onlinestore.dto.category.CreateCategoryDto;
import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> findAll();

    CategoryResponseDto getById(Long id);

    void deleteById(Long id);

    CategoryResponseDto save(CreateCategoryDto createCategoryDto);

    CategoryResponseDto update(Long id, CreateCategoryDto createCategoryDto);
}
