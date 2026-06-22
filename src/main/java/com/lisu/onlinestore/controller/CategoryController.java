package com.lisu.onlinestore.controller;

import com.lisu.onlinestore.dto.CategoryResponseDto;
import com.lisu.onlinestore.mapper.CategoryDtoMapper;
import com.lisu.onlinestore.model.Category;
import com.lisu.onlinestore.service.CategoryService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryDtoMapper categoryDtoMapper;

    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return categoryService.findAll().stream()
                .map(categoryDtoMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/inject")
    public String injectCategory() {
        Category category = new Category();
        category.setName("category-name");
        categoryService.save(category);
        return "Success!!";
    }
}
