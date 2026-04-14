package com.example.FakeCommerce.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.example.FakeCommerce.config.TestJpaConfig;
import com.example.FakeCommerce.schema.Category;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestJpaConfig.class)
public class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = Category.builder().name("Electronics").build();
        category2 = Category.builder().name("Clothing").build();

        testEntityManager.persistAndFlush(category1);
        testEntityManager.persistAndFlush(category2);

        testEntityManager.clear();
    }

    @Test
    void findAll_returnsAllCategories() {
        List<Category> result = categoryRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findById_whenFound_returnsCategory() {
        Optional<Category> result = categoryRepository.findById(category1.getId());

        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        Optional<Category> result = categoryRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void save_persistsCategory() {
        Category newCategory = Category.builder().name("Books").build();

        Category saved = categoryRepository.save(newCategory);

        assertEquals("Books", saved.getName());
        assertTrue(saved.getId() > 0);
    }

    @Test
    void delete_softDeletesCategory() {
        categoryRepository.deleteById(category1.getId());
        testEntityManager.flush();
        testEntityManager.clear();

        Optional<Category> result = categoryRepository.findById(category1.getId());

        assertFalse(result.isPresent());
    }
}
