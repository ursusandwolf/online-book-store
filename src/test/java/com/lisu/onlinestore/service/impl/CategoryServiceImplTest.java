package com.lisu.onlinestore.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lisu.onlinestore.dao.CategoryRepository;
import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import com.lisu.onlinestore.dto.category.CreateCategoryDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.CategoryMapper;
import com.lisu.onlinestore.model.Category;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void findAll_ShouldMapRepositoryPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Category category = createCategory(1L, "Fiction");
        CategoryResponseDto expected = createResponseDto(1L, "Fiction");
        Page<Category> categories = new PageImpl<>(List.of(category), pageable, 1);

        when(categoryRepository.findAll(pageable)).thenReturn(categories);
        when(categoryMapper.toResponseDto(category)).thenReturn(expected);

        Page<CategoryResponseDto> actual = categoryService.findAll(pageable);

        assertEquals(1, actual.getTotalElements());
        assertEquals(List.of(expected), actual.getContent());
    }

    @Test
    void getById_ShouldReturnMappedDto() {
        Category category = createCategory(2L, "Science");
        CategoryResponseDto expected = createResponseDto(2L, "Science");

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponseDto(category)).thenReturn(expected);

        CategoryResponseDto actual = categoryService.getById(2L);

        assertEquals(expected, actual);
    }

    @Test
    void getById_ShouldThrowWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(77L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.getById(77L)
        );

        assertEquals("Can't find category by id: 77", exception.getMessage());
    }

    @Test
    void save_ShouldReturnMappedDto() {
        CreateCategoryDto requestDto = createCategoryDto("History");
        Category category = createCategory(null, "History");
        Category savedCategory = createCategory(3L, "History");
        CategoryResponseDto expected = createResponseDto(3L, "History");

        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(savedCategory);
        when(categoryMapper.toResponseDto(savedCategory)).thenReturn(expected);

        CategoryResponseDto actual = categoryService.save(requestDto);

        assertEquals(expected, actual);
    }

    @Test
    void update_ShouldApplyChangesAndReturnMappedDto() {
        CreateCategoryDto requestDto = createCategoryDto("Biography");
        Category category = createCategory(4L, "Old name");
        CategoryResponseDto expected = createResponseDto(4L, "Biography");

        when(categoryRepository.findById(4L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponseDto(category)).thenReturn(expected);

        CategoryResponseDto actual = categoryService.update(4L, requestDto);

        assertEquals(expected, actual);
        verify(categoryMapper).updateFromDto(requestDto, category);
        verify(categoryRepository).save(category);
    }

    @Test
    void deleteById_ShouldDelegateToRepository() {
        categoryService.deleteById(6L);

        verify(categoryRepository).deleteById(6L);
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Description");
        return category;
    }

    private CategoryResponseDto createResponseDto(Long id, String name) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription("Description");
        return dto;
    }

    private CreateCategoryDto createCategoryDto(String name) {
        CreateCategoryDto dto = new CreateCategoryDto();
        dto.setName(name);
        dto.setDescription("Description");
        return dto;
    }
}
