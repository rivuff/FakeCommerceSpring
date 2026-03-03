package com.example.FakeCommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FakeCommerce.schema.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    
} 