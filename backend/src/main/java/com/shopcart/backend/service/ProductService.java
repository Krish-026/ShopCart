package com.shopcart.backend.service;

import com.shopcart.backend.dto.ProductRequestDto;
import com.shopcart.backend.dto.ProductResponseDto;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto dto);
    List<ProductResponseDto> getAllProducts();
    ProductResponseDto getProductById(Long id);
    List<ProductResponseDto> getProductsByCategory(Long categoryId);
    List<ProductResponseDto> searchProducts(String keyword);
    ProductResponseDto updateProduct(Long id, ProductRequestDto dto);
    void deleteProduct(Long id);
}
