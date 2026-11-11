package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.CategoryResponseDto;
import com.shopcart.backend.dto.ProductFilterCriteriaDto;
import com.shopcart.backend.dto.ProductRequestDto;
import com.shopcart.backend.dto.ProductResponseDto;
import com.shopcart.backend.entity.Category;
import com.shopcart.backend.entity.Product;
import com.shopcart.backend.exception.ErrorCode;
import com.shopcart.backend.exception.ResourceNotFoundException;
import com.shopcart.backend.repository.CategoryRepository;
import com.shopcart.backend.repository.ProductRepository;
import com.shopcart.backend.response.PageResponse;
import com.shopcart.backend.service.FileStorageService;
import com.shopcart.backend.service.ProductService;
import com.shopcart.backend.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

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
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: " + categoryId);
        }

        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);

        return PageResponse.from(productPage.map(this::mapToDto));
    }

    @Override
    @Transactional
    public PageResponse<ProductResponseDto> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts(pageable);
        }

        Page<Product> productPage = productRepository.searchProducts(keyword.trim(), pageable);

        return PageResponse.from(productPage.map(this::mapToDto));
    }

    @Override
    @Transactional
    @Caching(evict = {
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
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponseDto> filterProducts(ProductFilterCriteriaDto criteria, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.withFilters(criteria);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return PageResponse.from(productPage.map(this::mapToDto));
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "product_lists", allEntries = true)
    })
    public ProductResponseDto uploadProductImage(Long id, MultipartFile file) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "Product not found with ID: " + id
                ));

        // Remember the existing image
        String oldFileName = extractFileNameFromUrl(product.getImageUrl());
        // Store the new image first
        String storedFileName = fileStorageService.storeFile(file);

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            fileStorageService.deleteFile(storedFileName);
            throw new IllegalArgumentException("No active transaction synchronization available");
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (oldFileName != null && !oldFileName.isBlank() && !oldFileName.equals(storedFileName)) {
                            try {
                                fileStorageService.deleteFile(oldFileName);
                            } catch (Exception ex) {
                                log.error("Failed to delete old product image '{}' after commit: {}", oldFileName, ex.getMessage());
                            }
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            try {
                                fileStorageService.deleteFile(storedFileName);
                            } catch (Exception ex) {
                                log.error("Failed to cleanup newly stored file '{}' after rollback: {}", storedFileName, ex.getMessage());
                            }
                        }
                    }
                });

        String publicUrl = "/uploads/products/" + storedFileName;
        product.setImageUrl(publicUrl);

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }


    private ProductResponseDto mapToDto(Product product) {
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
                .imageUrl(product.getImageUrl())
                .category(categoryDto)
                .build();
    }

    private void deleteOldImageAfterCommit(String oldImageUrl) {
        if (oldImageUrl == null || oldImageUrl.isBlank()) {
            return;
        }

        String oldFileName = extractFileNameFromUrl(oldImageUrl);

        if (oldFileName == null || oldFileName.isBlank()) {
            return;
        }

        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            fileStorageService.deleteFile(oldFileName);
                        }
                    }
            );
        }
    }

    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        int lastSlash = url.lastIndexOf('/');

        return lastSlash != -1
                ? url.substring(lastSlash + 1)
                : url;
    }
}