package com.shopcart.backend.service;

import com.shopcart.backend.dto.ProductRequestDto;
import com.shopcart.backend.dto.ProductResponseDto;
import com.shopcart.backend.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto dto);
    PageResponse<ProductResponseDto> getAllProducts(Pageable pageable);
    ProductResponseDto getProductById(Long id);
    PageResponse<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable);
    PageResponse<ProductResponseDto> searchProducts(String keyword, Pageable pageable);
    ProductResponseDto updateProduct(Long id, ProductRequestDto dto);
    void deleteProduct(Long id);
}
