package com.lisu.onlinestore.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lisu.onlinestore.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = CategoryRepositoryTest.TestApplication.class)
class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByName_ShouldReturnCategory() {
        Category savedCategory = categoryRepository.save(createCategory("Fantasy"));

        Category actual = categoryRepository.findByName("Fantasy").orElseThrow();

        assertEquals(savedCategory.getId(), actual.getId());
        assertEquals("Fantasy", actual.getName());
    }

    @Test
    void existsByName_ShouldReturnTrueForPersistedCategory() {
        categoryRepository.save(createCategory("Poetry"));

        assertTrue(categoryRepository.existsByName("Poetry"));
    }

    @Test
    void deleteById_ShouldUseSoftDelete() {
        Category category = categoryRepository.save(createCategory("Drama"));

        categoryRepository.deleteById(category.getId());
        categoryRepository.flush();

        assertFalse(categoryRepository.findById(category.getId()).isPresent());
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Description");
        return category;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.lisu.onlinestore.model")
    @EnableJpaRepositories("com.lisu.onlinestore.dao")
    static class TestApplication {
    }
}
