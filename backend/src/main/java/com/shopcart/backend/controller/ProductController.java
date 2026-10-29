package com.shopcart.backend.controller;

import com.shopcart.backend.dto.ProductRequestDto;
import com.shopcart.backend.dto.ProductResponseDto;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.PageResponse;
import com.shopcart.backend.response.ResponseUtil;
import com.shopcart.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints for managing products, catalog search, and pagination")
public class ProductController {
    private final ProductService productService;

    // PUBLIC: Get all products
    @GetMapping
    @Operation(summary = "Get All Products (Paginated & Sorted)")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ){
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        PageResponse<ProductResponseDto> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(
                ResponseUtil.success(response, "Products retrieve successfully")
        );
    }

    // PUBLIC: Get product by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(@PathVariable Long id){
        ProductResponseDto response = productService.getProductById(id);
        return ResponseEntity.ok(
                ResponseUtil.success(response, "Products retrieve successfully")
        );
    }

    // PUBLIC: Get products by Category ID
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by Category ID")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ){
        Pageable pageable = createPageable(page,size, sortBy, sortDir);

        PageResponse<ProductResponseDto> response = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(
                ResponseUtil.success(response, "Products retrieve successfully")
        );
    }

    // PUBLIC: Search products by keyword
    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ){
        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        PageResponse<ProductResponseDto> response = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(
                ResponseUtil.success(response, "Products matching search criteria")
        );
    }

    // ADMIN ONLY: Create product
    @PostMapping
    @Operation(summary = "Create Product")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(@Valid @RequestBody ProductRequestDto dto){
        ProductResponseDto response = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(response, "Product created successfully"));
    }

    // ADMIN ONLY: Update product
    @PutMapping("/{id}")
    @Operation(summary = "Update Product")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDto dto){
        ProductResponseDto response = productService.updateProduct(id, dto);
        return ResponseEntity.ok(ResponseUtil.success(response, "Product updated successfully"));
    }

    // ADMIN ONLY: Delete product
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.ok(ResponseUtil.success(null, "Product deleted successfully"));
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}
