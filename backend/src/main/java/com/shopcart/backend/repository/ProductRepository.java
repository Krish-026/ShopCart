package com.shopcart.backend.repository;

import com.shopcart.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long cagtegoryId);

    List<Product> findByNameContainingIgnoreCase(String keyword);
}
