package com.lisu.onlinestore.controller;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dao.CategoryRepository;
import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.CategoryResponseDto;
import com.lisu.onlinestore.model.Category;
import com.lisu.onlinestore.service.BookService;
import com.lisu.onlinestore.service.CategoryService;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final BookService bookService;

    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return categoryService.findAll();
    }

    //GET /categories/{id}/books
    @GetMapping("{id}/books")
    public Page<BookDto> getAllBooksByCategoryId(@PathVariable Long id, Pageable pageable) {
        return bookService.findAllByCategoriesId(id, pageable);
    }
}
