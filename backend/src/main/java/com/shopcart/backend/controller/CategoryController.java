package com.shopcart.backend.controller;

import com.shopcart.backend.dto.CategoryRequestDto;
import com.shopcart.backend.dto.CategoryResponseDto;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.ResponseUtil;
import com.shopcart.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ===========================================
    // PUBLIC APIs
    // ===========================================

    // Get all categories
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(
                ResponseUtil.success(categories, "Categories retrieved successfully")
        );
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(
                ResponseUtil.success(category, "Category retrieved successfully")
        );
    }

    // Get category by slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponseDto category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(
                ResponseUtil.success(category, "Category retrieved successfully")
        );
    }

    // ===========================================
    // ADMIN APIs
    // ===========================================

    // Create category
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(@Valid @RequestBody CategoryRequestDto dto) {
        CategoryResponseDto response = categoryService.createCategory(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ResponseUtil.success(
                                response,
                                "Category created successfully"
                        )
                );
    }

    // Update Category
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDto dto
    ) {
        CategoryResponseDto updatedCategory = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(
                ResponseUtil.success(updatedCategory, "Category updated successfully")
        );
    }

    // Delete category
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
                ResponseUtil.success(null, "Category deleted successfully")
        );
    }
}
