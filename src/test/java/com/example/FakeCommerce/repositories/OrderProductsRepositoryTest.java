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
import com.example.FakeCommerce.schema.OrderProducts;
import com.example.FakeCommerce.schema.OrderStatus;
import com.example.FakeCommerce.schema.Product;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestJpaConfig.class)
public class OrderProductsRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private OrderproductsRepository orderProductsRepository;

    private Order order1;
    private Order order2;
    private Product product1;
    private Product product2;
    private OrderProducts orderProduct1;
    private OrderProducts orderProduct2;
    private OrderProducts orderProduct3;

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

        order1 = Order.builder().status(OrderStatus.PENDING).build();
        order2 = Order.builder().status(OrderStatus.SHIPPED).build();

        testEntityManager.persistAndFlush(order1);
        testEntityManager.persistAndFlush(order2);

        orderProduct1 = OrderProducts.builder()
            .order(order1)
            .product(product1)
            .quantity(2)
            .build();
        orderProduct2 = OrderProducts.builder()
            .order(order1)
            .product(product2)
            .quantity(1)
            .build();
        orderProduct3 = OrderProducts.builder()
            .order(order2)
            .product(product1)
            .quantity(3)
            .build();

        testEntityManager.persistAndFlush(orderProduct1);
        testEntityManager.persistAndFlush(orderProduct2);
        testEntityManager.persistAndFlush(orderProduct3);

        testEntityManager.clear();
    }

    @Test
    void findByOrderId_returnsMatchingOrderProducts() {
        List<OrderProducts> result = orderProductsRepository.findByOrderId(order1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(op -> op.getOrder().getId().equals(order1.getId())));
    }

    @Test
    void findByOrderId_whenNoProducts_returnsEmptyList() {
        Order emptyOrder = Order.builder().status(OrderStatus.CANCELLED).build();
        testEntityManager.persistAndFlush(emptyOrder);
        testEntityManager.clear();

        List<OrderProducts> result = orderProductsRepository.findByOrderId(emptyOrder.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByOrderWithProduct_returnsOrderProductsWithFetchedProduct() {
        List<OrderProducts> result = orderProductsRepository.findByOrderWithProduct(order1);

        assertEquals(2, result.size());
        assertEquals("Phone", result.stream()
            .filter(op -> op.getProduct().getId().equals(product1.getId()))
            .findFirst().get().getProduct().getTitle());
        assertEquals("Laptop", result.stream()
            .filter(op -> op.getProduct().getId().equals(product2.getId()))
            .findFirst().get().getProduct().getTitle());
    }

    @Test
    void findByOrderWithProduct_whenNoProducts_returnsEmptyList() {
        Order emptyOrder = Order.builder().status(OrderStatus.CANCELLED).build();
        testEntityManager.persistAndFlush(emptyOrder);
        testEntityManager.clear();

        List<OrderProducts> result = orderProductsRepository.findByOrderWithProduct(emptyOrder);

        assertTrue(result.isEmpty());
    }
}
