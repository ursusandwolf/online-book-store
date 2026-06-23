package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.CategoryRepository;
import com.lisu.onlinestore.dto.category.CategoryDto;
import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.CategoryMapper;
import com.lisu.onlinestore.model.Category;
import com.lisu.onlinestore.service.CategoryService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDto getById(Long id) {
        // ищем категорию и кидаем ошибку, если не нашли
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find category by id: " + id));
        return categoryMapper.toResponseDto(category);
    }

    @Override
    public List<CategoryResponseDto> findAll() {
        log.info("Trying to get all categories.");
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    @Override
    public CategoryResponseDto save(CategoryDto categoryDto) {
        // DTO -> entity -> save -> response DTO
        Category category = categoryMapper.toEntity(categoryDto);
        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }

    @Override
    public void deleteById(Long id) {
        // soft delete уже настроен в entity
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponseDto update(Long id, CategoryDto categoryDto) {
        // обновляем только нужные поля
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find category by id: " + id));
        category.setName(categoryDto.name());
        category.setDescription(categoryDto.description());
        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }
}
