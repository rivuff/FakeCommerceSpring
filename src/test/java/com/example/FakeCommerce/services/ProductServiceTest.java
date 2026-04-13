package com.example.FakeCommerce.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.FakeCommerce.dtos.CreateProductRequestDto;
import com.example.FakeCommerce.dtos.GetProductResponseDto;
import com.example.FakeCommerce.dtos.GetProductWithDetailsResponseDto;
import com.example.FakeCommerce.exceptions.ResourceNotFoundException;
import com.example.FakeCommerce.repositories.ProductRepository;
import com.example.FakeCommerce.schema.Category;
import com.example.FakeCommerce.schema.Product;
import com.example.FakeCommerce.services.cache.ProductRedisCache;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductRedisCache productRedisCache;

    @InjectMocks
    private ProductService productService;

    private Product buildProduct(Long id, String title, BigDecimal price, BigDecimal rating, Category category) {
        Product product = Product.builder()
                .title(title)
                .description(title + " description")
                .price(price)
                .image("http://img/" + id + ".png")
                .rating(rating)
                .category(category)
                .build();
        product.setId(id);
        return product;
    }

    // --- getAllProducts ---

    @Test
    void getAllProducts_returnsMappedDtoList() {
        // arrange.
        Product p1 = buildProduct(1L, "Phone", BigDecimal.valueOf(999), BigDecimal.valueOf(4.5), null);
        Product p2 = buildProduct(2L, "Laptop", BigDecimal.valueOf(1999), BigDecimal.valueOf(4.8), null);
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        // act.
        List<GetProductResponseDto> result = productService.getAllProducts();

        // assert.
        assertEquals(2, result.size());
        assertEquals("Phone", result.get(0).getTitle());
        assertEquals(BigDecimal.valueOf(999), result.get(0).getPrice());
        assertEquals("Laptop", result.get(1).getTitle());
    }

    @Test
    void getAllProducts_whenEmpty_returnsEmptyList() {
        // arrange.
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // act.
        List<GetProductResponseDto> result = productService.getAllProducts();

        // assert.
        assertTrue(result.isEmpty());
    }

    // --- getProductById ---

    @Test
    void getProductById_cacheHit_returnsCachedDto() {
        // arrange.
        GetProductResponseDto cached = GetProductResponseDto.builder()
                .id(1L).title("Cached Phone").price(BigDecimal.valueOf(500)).build();
        when(productRedisCache.getSummary(1L)).thenReturn(Optional.of(cached));

        // act.
        GetProductResponseDto result = productService.getProductById(1L);

        // assert.
        assertEquals("Cached Phone", result.getTitle());
        assertEquals(BigDecimal.valueOf(500), result.getPrice());
    }

    @Test
    void getProductById_cacheMiss_fetchesFromDbAndCaches() {
        // arrange.
        when(productRedisCache.getSummary(1L)).thenReturn(Optional.empty());
        Product product = buildProduct(1L, "Phone", BigDecimal.valueOf(999), BigDecimal.valueOf(4.5), null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // act.
        GetProductResponseDto result = productService.getProductById(1L);

        // assert.
        assertEquals("Phone", result.getTitle());
        assertEquals(1L, result.getId());
        verify(productRedisCache).putSummary(eq(1L), any(GetProductResponseDto.class));
    }

    @Test
    void getProductById_notFound_throwsResourceNotFoundException() {
        // arrange.
        when(productRedisCache.getSummary(99L)).thenReturn(Optional.empty());
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    // --- getProductWithDetailsById ---

    @Test
    void getProductWithDetailsById_whenFound_returnsDtoWithCategory() {
        // arrange.
        Category category = Category.builder().name("Electronics").build();
        category.setId(1L);
        Product product = buildProduct(1L, "Phone", BigDecimal.valueOf(999), BigDecimal.valueOf(4.5), category);
        when(productRepository.findProductWithDetailsById(1L)).thenReturn(List.of(product));

        // act.
        GetProductWithDetailsResponseDto result = productService.getProductWithDetailsById(1L);

        // assert.
        assertEquals("Phone", result.getTitle());
        assertEquals("Electronics", result.getCategory());
        assertEquals(BigDecimal.valueOf(999), result.getPrice());
    }

    @Test
    void getProductWithDetailsById_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(productRepository.findProductWithDetailsById(99L)).thenReturn(Collections.emptyList());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductWithDetailsById(99L));
    }

    // --- createProduct ---

    @Test
    void createProduct_savesAndReturnsProduct() {
        // arrange.
        Category category = Category.builder().name("Electronics").build();
        category.setId(1L);
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        CreateProductRequestDto dto = CreateProductRequestDto.builder()
                .title("Phone")
                .description("A phone")
                .image("http://img/phone.png")
                .price(BigDecimal.valueOf(999))
                .categoryId(1L)
                .rating(BigDecimal.valueOf(4.5))
                .build();

        Product savedProduct = buildProduct(1L, "Phone", BigDecimal.valueOf(999), BigDecimal.valueOf(4.5), category);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // act.
        Product result = productService.createProduct(dto);

        // assert.
        assertEquals("Phone", result.getTitle());
        assertEquals(1L, result.getId());
        assertEquals(category, result.getCategory());
    }

    @Test
    void createProduct_categoryNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(categoryService.getCategoryById(99L))
                .thenThrow(new ResourceNotFoundException("Category with id 99 not found"));

        CreateProductRequestDto dto = CreateProductRequestDto.builder()
                .title("Phone")
                .categoryId(99L)
                .price(BigDecimal.valueOf(999))
                .rating(BigDecimal.valueOf(4.0))
                .build();

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(dto));
    }

    // --- deleteProduct ---

    @Test
    void deleteProduct_whenFound_deletesSuccessfully() {
        // arrange.
        Product product = buildProduct(1L, "Phone", BigDecimal.valueOf(999), BigDecimal.valueOf(4.5), null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // act.
        productService.deleteProduct(1L);

        // assert.
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99L));
    }

    // --- getProductsByCategory ---

    @Test
    void getProductsByCategory_returnsList() {
        // arrange.
        Product p1 = buildProduct(1L, "Phone", BigDecimal.valueOf(999), BigDecimal.valueOf(4.5), null);
        Product p2 = buildProduct(2L, "Tablet", BigDecimal.valueOf(599), BigDecimal.valueOf(4.2), null);
        when(productRepository.findByCategory("Electronics")).thenReturn(List.of(p1, p2));

        // act.
        List<Product> result = productService.getProductsByCategory("Electronics");

        // assert.
        assertEquals(2, result.size());
    }

    @Test
    void getProductsByCategory_whenNoneMatch_returnsEmptyList() {
        // arrange.
        when(productRepository.findByCategory("NonExistent")).thenReturn(Collections.emptyList());

        // act.
        List<Product> result = productService.getProductsByCategory("NonExistent");

        // assert.
        assertTrue(result.isEmpty());
    }

    // --- getAllCategories (from ProductService) ---

    @Test
    void getAllCategories_returnsDistinctCategoryNames() {
        // arrange.
        when(productRepository.findAllCategories()).thenReturn(List.of("Electronics", "Clothing"));

        // act.
        List<String> result = productService.getAllCategories();

        // assert.
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0));
        assertEquals("Clothing", result.get(1));
    }
}
