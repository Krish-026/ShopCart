package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.CategoryResponseDto;
import com.shopcart.backend.dto.ProductRequestDto;
import com.shopcart.backend.dto.ProductResponseDto;
import com.shopcart.backend.entity.Category;
import com.shopcart.backend.entity.Product;
import com.shopcart.backend.exception.ErrorCode;
import com.shopcart.backend.exception.ResourceNotFoundException;
import com.shopcart.backend.repository.CategoryRepository;
import com.shopcart.backend.repository.ProductRepository;
import com.shopcart.backend.response.PageResponse;
import com.shopcart.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @CacheEvict(value = "product_lists", allEntries = true)
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: " + dto.getCategoryId()));

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponseDto> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return PageResponse.from(productPage.map(this::mapToDto));
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + id));
        return mapToDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        if(!categoryRepository.existsById(categoryId)){
            throw new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: "+ categoryId);
        }

        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);

        return PageResponse.from(productPage.map(this::mapToDto));
    }

    @Override
    @Transactional
    public PageResponse<ProductResponseDto> searchProducts(String keyword, Pageable pageable) {
        if(keyword == null || keyword.trim().isEmpty()){
            return getAllProducts(pageable);
        }

        Page<Product> productPage = productRepository.searchProducts(keyword.trim(), pageable);

        return PageResponse.from(productPage.map(this::mapToDto));
    }

    @Override
    @Transactional
    @Caching( evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "product_lists", allEntries = true)
        }
    )
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: " + dto.getCategoryId()));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "product_lists", allEntries = true)
    })
    public void deleteProduct(Long id) {
        if(!productRepository.existsById(id)){
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }


    private ProductResponseDto mapToDto(Product product){
        CategoryResponseDto categoryDto = CategoryResponseDto.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .slug(product.getCategory().getSlug())
                .build();

        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(categoryDto)
                .build();
    }
}
