package com.lisu.onlinestore.service;

import com.lisu.onlinestore.model.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAll();

    Category save(Category category);
}
