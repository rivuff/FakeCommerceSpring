package com.example.FakeCommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FakeCommerce.schema.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
}