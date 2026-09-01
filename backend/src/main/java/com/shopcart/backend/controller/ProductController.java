package com.shopcart.backend.controller;

import com.shopcart.backend.dto.ProductRequestDto;
import com.shopcart.backend.dto.ProductResponseDto;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.ResponseUtil;
import com.shopcart.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // PUBLIC: Get all products
    @GetMapping
    @Operation(summary = "Get All Products")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAllProducts(){
        List<ProductResponseDto> response = productService.getAllProducts();
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
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductsByCategory(@PathVariable Long categoryId){
        List<ProductResponseDto> response = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(
                ResponseUtil.success(response, "Products retrieve successfully")
        );
    }

    // PUBLIC: Search products by keyword
    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> searchProducts(@RequestParam String query){
        List<ProductResponseDto> response = productService.searchProducts(query);
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
}
