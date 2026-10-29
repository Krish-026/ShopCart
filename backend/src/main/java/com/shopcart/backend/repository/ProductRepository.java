package com.shopcart.backend.repository;

import com.shopcart.backend.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    List<Product> findByCategorySlug(String categorySlug);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByStockQuantityGreaterThan(Integer minStock);

    // Search products by name keyword or category name
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
