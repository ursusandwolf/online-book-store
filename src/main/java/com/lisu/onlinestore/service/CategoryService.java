package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.CategoryResponseDto;
import com.lisu.onlinestore.model.Category;
import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> findAll();

    Category save(Category category);
}
