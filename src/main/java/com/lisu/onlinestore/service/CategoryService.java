package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import com.lisu.onlinestore.dto.category.CreateCategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    Page<CategoryResponseDto> findAll(Pageable pageable);

    CategoryResponseDto getById(Long id);

    void deleteById(Long id);

    CategoryResponseDto save(CreateCategoryDto createCategoryDto);

    CategoryResponseDto update(Long id, CreateCategoryDto createCategoryDto);
}
