package com.lisu.onlinestore.controller;

import com.lisu.onlinestore.dto.book.BookDtoWithoutCategoryIds;
import com.lisu.onlinestore.dto.category.CategoryDto;
import com.lisu.onlinestore.dto.category.CategoryResponseDto;
import com.lisu.onlinestore.service.BookService;
import com.lisu.onlinestore.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "Category", description = "Operations with book categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final BookService bookService;

    // USER endpoints -----------------------------------------------------
    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single category")
    public CategoryResponseDto getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @GetMapping("/{id}/books")
    @Operation(summary = "Get books of a category")
    public Page<BookDtoWithoutCategoryIds> getBooks(@PathVariable Long id, Pageable pageable) {
        return bookService.findAllByCategoriesId(id, pageable);
    }

    // ADMIN endpoints -----------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new category")
    public CategoryResponseDto create(@RequestBody @Valid CategoryDto dto) {
        return categoryService.save(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public CategoryResponseDto update(
            @PathVariable Long id, @RequestBody @Valid CategoryDto dto) {
        return categoryService.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public void delete(@PathVariable Long id) {
        categoryService.deleteById(id);
    }
}
