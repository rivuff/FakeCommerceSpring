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
import com.example.FakeCommerce.schema.Order;
import com.example.FakeCommerce.schema.OrderStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestJpaConfig.class)
public class OrderRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private OrderRespository orderRepository;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        order1 = Order.builder().status(OrderStatus.PENDING).build();
        order2 = Order.builder().status(OrderStatus.SHIPPED).build();

        testEntityManager.persistAndFlush(order1);
        testEntityManager.persistAndFlush(order2);

        testEntityManager.clear();
    }

    @Test
    void findAll_returnsAllOrders() {
        List<Order> result = orderRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findById_whenFound_returnsOrder() {
        Optional<Order> result = orderRepository.findById(order1.getId());

        assertTrue(result.isPresent());
        assertEquals(OrderStatus.PENDING, result.get().getStatus());
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        Optional<Order> result = orderRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void save_persistsOrder() {
        Order newOrder = Order.builder().status(OrderStatus.DELIVERED).build();

        Order saved = orderRepository.save(newOrder);

        assertEquals(OrderStatus.DELIVERED, saved.getStatus());
        assertTrue(saved.getId() > 0);
    }

    @Test
    void delete_softDeletesOrder() {
        orderRepository.deleteById(order1.getId());
        testEntityManager.flush();
        testEntityManager.clear();

        Optional<Order> result = orderRepository.findById(order1.getId());

        assertFalse(result.isPresent());
    }
}
