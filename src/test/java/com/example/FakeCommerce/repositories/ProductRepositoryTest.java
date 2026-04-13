package com.example.FakeCommerce.repositories;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.example.FakeCommerce.config.TestJpaConfig;
import com.example.FakeCommerce.schema.Category;
import com.example.FakeCommerce.schema.Product;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestJpaConfig.class)
public class ProductRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ProductRepository productRepository;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        // arrange.
        category = Category.builder().name("Electronics").build();
        product = Product.builder()
            .title("Phone")
            .description("A phone")
            .price(BigDecimal.valueOf(999.0).setScale(2, RoundingMode.HALF_UP))
            .rating(BigDecimal.valueOf(4.5).setScale(2, RoundingMode.HALF_UP))
            .category(category)
            .build();

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product);

        testEntityManager.clear();
    }

    @Test
    void findProductWithDetailsById_whenFound_returnsProductWithCategory() {
        // act.
        List<Product> result = productRepository.findProductWithDetailsById(product.getId());

        // assert.
        assertEquals(1, result.size());
        assertEquals(category, result.get(0).getCategory());
        assertEquals(product.getTitle(), result.get(0).getTitle());
        assertEquals(product.getDescription(), result.get(0).getDescription());
        assertEquals(product.getPrice(), result.get(0).getPrice());
        assertEquals(product.getRating(), result.get(0).getRating());
        assertEquals(product.getImage(), result.get(0).getImage());
        assertEquals(product.getCategory().getName(), result.get(0).getCategory().getName());
    }

}
