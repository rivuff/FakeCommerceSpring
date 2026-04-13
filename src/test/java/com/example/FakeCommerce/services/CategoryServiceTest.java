package com.example.FakeCommerce.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.FakeCommerce.dtos.CreateCategoryRequestDto;
import com.example.FakeCommerce.exceptions.ResourceNotFoundException;
import com.example.FakeCommerce.repositories.CategoryRepository;
import com.example.FakeCommerce.schema.Category;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;


    @Test
    void createCategory_savesAndReturnsCategory() {
        // arrange.
        CreateCategoryRequestDto dto = CreateCategoryRequestDto.builder().name("Test Category").build();
        Category testCategory = Category.builder().name("Test Category").build();
        testCategory.setId(1L);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // act.
        Category result = categoryService.createCategory(dto);

        // assert.
        assertEquals("Test Category", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void getCategoryById_whenFound_returnsCategory() {
        // arrange.
        Category testCategory = Category.builder().name("Test Category").build();
        testCategory.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // act.
        Category result = categoryService.getCategoryById(1L);

        // assert.
        assertEquals("Test Category", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void getCategoryById_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(2L));
    }

    @Test
    void getAllCategories_returnsList() {
        // arrange.
        Category cat1 = Category.builder().name("Electronics").build();
        cat1.setId(1L);
        Category cat2 = Category.builder().name("Clothing").build();
        cat2.setId(2L);
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        // act.
        List<Category> result = categoryService.getAllCategories();

        // assert.
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName());
        assertEquals("Clothing", result.get(1).getName());
    }

    @Test
    void getAllCategories_whenEmpty_returnsEmptyList() {
        // arrange.
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        // act.
        List<Category> result = categoryService.getAllCategories();

        // assert.
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteCategory_whenFound_deletesSuccessfully() {
        // arrange.
        Category testCategory = Category.builder().name("Test Category").build();
        testCategory.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // act.
        categoryService.deleteCategory(1L);

        // assert.
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void deleteCategory_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));
    }
}
