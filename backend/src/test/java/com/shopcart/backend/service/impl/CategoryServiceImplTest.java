package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.CategoryRequestDto;
import com.shopcart.backend.dto.CategoryResponseDto;
import com.shopcart.backend.entity.Category;
import com.shopcart.backend.exception.ResourceNotFoundException;
import com.shopcart.backend.repository.CategoryRepository;
import com.shopcart.backend.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_ShouldReturnCategoryResponse_WhenCategoryIsValid(){
        //Arrange
        CategoryRequestDto request = new CategoryRequestDto();
        request.setName("Electronics");

        when(categoryRepository.existsByNameIgnoreCase("Electronics"))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category category = invocation.getArgument(0);
                    category.setId(1L);
                    return category;
                });

        // Act
        CategoryResponseDto response = categoryService.createCategory(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Electronics", response.getName());
        assertEquals("electronics", response.getSlug());

        verify(categoryRepository).existsByNameIgnoreCase("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowException_WhenCategoryAlreadyExists(){
        // Arrange
        CategoryRequestDto request = new CategoryRequestDto();
        request.setName("Electronics");

        when(categoryRepository.existsByNameIgnoreCase("Electronics"))
                .thenReturn(true);

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> categoryService.createCategory(request)
        );

        // Assert
        assertEquals(
                "Category with name 'Electronics' already exists",
                exception.getMessage()
        );

        verify(categoryRepository).existsByNameIgnoreCase("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getAllCategories_ShouldReturnCategoryList_WhenCategoriesExist() {
        // Arrange
        Category category1 = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .build();

        Category category2 = Category.builder()
                .id(2L)
                .name("Clothing")
                .slug("clothing")
                .build();

        when(categoryRepository.findAll())
                .thenReturn(List.of(category1, category2));

        // Act
        List<CategoryResponseDto> response = categoryService.getAllCategories();

        // Assert
        assertEquals(2, response.size());

        assertEquals(1L, response.get(0).getId());
        assertEquals("Electronics", response.get(0).getName());
        assertEquals("electronics", response.get(0).getSlug());

        assertEquals(2L, response.get(1).getId());
        assertEquals("Clothing", response.get(1).getName());
        assertEquals("clothing", response.get(1).getSlug());

        verify(categoryRepository).findAll();
    }

    @Test
    void getAllCategories_ShouldReturnEmptyList_WhenNoCategoriesExist() {
        // Arrange
        when(categoryRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<CategoryResponseDto> response = categoryService.getAllCategories();

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenCategoryExists(){
        // Arrange
        Long categoryId = 1L;

        Category category = Category.builder()
                .id(categoryId)
                .name("Electronics")
                .slug("electronics")
                .build();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        // Act
        CategoryResponseDto response = categoryService.getCategoryById(categoryId);

        // Assert
        assertNotNull(response);
        assertEquals(categoryId, response.getId());
        assertEquals("Electronics", response.getName());
        assertEquals("electronics", response.getSlug());

        verify(categoryRepository).findById(categoryId);
    }


    @Test
    void getCategoryById_ShouldThrowException_WhenCategoryDoesNotExist() {

        // Arrange
        Long categoryId = 99L;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(categoryId)
        );

        verify(categoryRepository).findById(categoryId);
    }

}


