package com.shopcart.backend.service;

import com.shopcart.backend.dto.CartItemRequestDto;
import com.shopcart.backend.dto.CartResponseDto;

public interface CartService {

    CartResponseDto getCart(String userEmail);
    CartResponseDto addItemToCart(String userEmail, CartItemRequestDto request);
    CartResponseDto updateItemQuantity(String userEmail, Long itemId, Integer quantity);
    CartResponseDto removeItemFromCart(String userEmail, Long itemId);
    void clearCart(String userEmail);
}
