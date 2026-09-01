package com.shopcart.backend.controller;

import com.shopcart.backend.dto.CartItemRequestDto;
import com.shopcart.backend.dto.CartResponseDto;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.ResponseUtil;
import com.shopcart.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    // View current user's cart
    @GetMapping
    @Operation(summary = "Get current user's car")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(@AuthenticationPrincipal UserDetails userDetails){
        CartResponseDto cart = cartService.getCart(userDetails.getUsername());
        return ResponseEntity.ok(ResponseUtil.success(cart, "Cart retrieved successfully"));
    }

    // Add item to cart
    @PostMapping("/items")
    @Operation(summary = "Add an item to cart")
    public ResponseEntity<ApiResponse<CartResponseDto>> addItemToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequestDto request
            ){
        CartResponseDto cart = cartService.addItemToCart(userDetails.getUsername(), request);
        return ResponseEntity.ok(ResponseUtil.success(cart, "Item added to cart successfully"));
    }

    // Update quantity of an item
    @PutMapping("items/{itemId}")
    @Operation(summary = "Update an item")
    public ResponseEntity<ApiResponse<CartResponseDto>> updateItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam Integer quantity
    ){
        CartResponseDto cart = cartService.updateItemQuantity(userDetails.getUsername(), itemId, quantity);
        return ResponseEntity.ok(ResponseUtil.success(cart, "Cart item updated successfully"));
    }

    // Remove single item from cart
    @DeleteMapping("items/{itemId}")
    @Operation(summary = "Remove an single item")
    public ResponseEntity<ApiResponse<CartResponseDto>> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId
    ){
        CartResponseDto cart = cartService.removeItemFromCart(userDetails.getUsername(), itemId);
        return ResponseEntity.ok(ResponseUtil.success(cart, "Item removed from cart"));
    }

    //Clear entire cart
    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(ResponseUtil.success(null, "Cart cleared successfully"));
    }
}