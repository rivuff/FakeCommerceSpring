package com.example.FakeCommerce.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.example.FakeCommerce.schema.Order;
import com.example.FakeCommerce.schema.OrderStatus;
import com.example.FakeCommerce.schema.Product;
import com.example.FakeCommerce.schema.Review;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestJpaConfig.class)
public class ReviewRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private Product product1;
    private Product product2;
    private Order order1;
    private Order order2;
    private Review review1;
    private Review review2;
    private Review review3;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().name("Electronics").build();
        testEntityManager.persistAndFlush(category);

        product1 = Product.builder()
            .title("Phone")
            .description("A phone")
            .price(BigDecimal.valueOf(999.00).setScale(2, RoundingMode.HALF_UP))
            .rating(BigDecimal.valueOf(4.5).setScale(2, RoundingMode.HALF_UP))
            .category(category)
            .build();
        product2 = Product.builder()
            .title("Laptop")
            .description("A laptop")
            .price(BigDecimal.valueOf(1999.00).setScale(2, RoundingMode.HALF_UP))
            .rating(BigDecimal.valueOf(4.0).setScale(2, RoundingMode.HALF_UP))
            .category(category)
            .build();

        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        order1 = Order.builder().status(OrderStatus.DELIVERED).build();
        order2 = Order.builder().status(OrderStatus.DELIVERED).build();

        testEntityManager.persistAndFlush(order1);
        testEntityManager.persistAndFlush(order2);

        review1 = Review.builder()
            .comment("Great phone!")
            .rating(BigDecimal.valueOf(5.0).setScale(2, RoundingMode.HALF_UP))
            .product(product1)
            .order(order1)
            .build();
        review2 = Review.builder()
            .comment("Decent phone")
            .rating(BigDecimal.valueOf(3.0).setScale(2, RoundingMode.HALF_UP))
            .product(product1)
            .order(order2)
            .build();
        review3 = Review.builder()
            .comment("Amazing laptop")
            .rating(BigDecimal.valueOf(4.5).setScale(2, RoundingMode.HALF_UP))
            .product(product2)
            .order(order1)
            .build();

        testEntityManager.persistAndFlush(review1);
        testEntityManager.persistAndFlush(review2);
        testEntityManager.persistAndFlush(review3);

        testEntityManager.clear();
    }

    @Test
    void findByProductId_returnsMatchingReviews() {
        List<Review> result = reviewRepository.findByProductId(product1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getProduct().getId().equals(product1.getId())));
    }

    @Test
    void findByProductId_whenNoReviews_returnsEmptyList() {
        Category category = Category.builder().name("Books").build();
        testEntityManager.persistAndFlush(category);

        Product productWithNoReviews = Product.builder()
            .title("Book")
            .description("A book")
            .price(BigDecimal.valueOf(19.99).setScale(2, RoundingMode.HALF_UP))
            .rating(BigDecimal.valueOf(0.0).setScale(2, RoundingMode.HALF_UP))
            .category(category)
            .build();
        testEntityManager.persistAndFlush(productWithNoReviews);
        testEntityManager.clear();

        List<Review> result = reviewRepository.findByProductId(productWithNoReviews.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByOrderId_returnsMatchingReviews() {
        List<Review> result = reviewRepository.findByOrderId(order1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getOrder().getId().equals(order1.getId())));
    }

    @Test
    void findByOrderId_whenNoReviews_returnsEmptyList() {
        Order orderWithNoReviews = Order.builder().status(OrderStatus.PENDING).build();
        testEntityManager.persistAndFlush(orderWithNoReviews);
        testEntityManager.clear();

        List<Review> result = reviewRepository.findByOrderId(orderWithNoReviews.getId());

        assertTrue(result.isEmpty());
    }
}
