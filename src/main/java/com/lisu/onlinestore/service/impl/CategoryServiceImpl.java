package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.CategoryRepository;
import com.lisu.onlinestore.model.Category;
import com.lisu.onlinestore.service.CategoryService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        log.info("Trying to get all categories.");
        return categoryRepository.findAll();
    }

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }
}
