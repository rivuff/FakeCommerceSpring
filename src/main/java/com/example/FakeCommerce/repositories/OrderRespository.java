package com.example.FakeCommerce.repositories;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.FakeCommerce.schema.Order;

public interface OrderRespository extends JpaRepository<Order, Long> {

    
}
