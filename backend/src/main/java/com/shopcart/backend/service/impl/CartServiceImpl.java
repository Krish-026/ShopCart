package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.CartItemRequestDto;
import com.shopcart.backend.dto.CartItemResponseDto;
import com.shopcart.backend.dto.CartResponseDto;
import com.shopcart.backend.entity.Cart;
import com.shopcart.backend.entity.CartItem;
import com.shopcart.backend.entity.Product;
import com.shopcart.backend.entity.User;
import com.shopcart.backend.exception.ErrorCode;
import com.shopcart.backend.exception.ResourceNotFoundException;
import com.shopcart.backend.repository.CartRepository;
import com.shopcart.backend.repository.ProductRepository;
import com.shopcart.backend.repository.UserRepository;
import com.shopcart.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponseDto getCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        return mapToCartResponseDto(cart);
    }

    @Override
    @Transactional
    public CartResponseDto addItemToCart(String userEmail, CartItemRequestDto request) {
        Cart cart = getOrCreateCart(userEmail);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(()-> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + request.getProductId()));

        if(product.getStockQuantity() < request.getQuantity()){
            throw new ResourceNotFoundException(ErrorCode.INSUFFICIENT_STOCK, "Requested quantity exceeds available stock (" + product.getStockQuantity() + ")");
        }

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if(existingItem.isPresent()){
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            if(product.getStockQuantity() < newQuantity){
                throw new ResourceNotFoundException(ErrorCode.INSUFFICIENT_STOCK, "Cannot add more. Exceeds total available stock");
            }

            item.setQuantity(newQuantity);
            item.setSubTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
        } else {
            BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .subTotal(subTotal)
                    .build();
            cart.getCartItems().add(newItem);
        }

        recalculateCartTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponseDto(savedCart);
    }

    @Override
    @Transactional
    public CartResponseDto updateItemQuantity(String userEmail, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Item not found in cart with ID: " + itemId));

        if(quantity <= 0){
            cart.getCartItems().remove(item);
        } else {
            if(item.getProduct().getStockQuantity() < quantity){
                throw new ResourceNotFoundException(ErrorCode.INSUFFICIENT_STOCK, "Requested quantity exceeds available stock (" + item.getProduct().getStockQuantity() +")");
            }
            item.setQuantity(quantity);
            item.setSubTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        recalculateCartTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponseDto(savedCart);
    }

    @Override
    @Transactional
    public CartResponseDto removeItemFromCart(String userEmail, Long itemId) {
        Cart cart = getOrCreateCart(userEmail);

        boolean removed = cart.getCartItems().removeIf(item -> item.getId().equals(itemId));
        if(!removed){
            throw new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Item not found in cart with ID; " + itemId);
        }

        recalculateCartTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponseDto(savedCart);
    }

    @Override
    public void clearCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new ResourceNotFoundException(ErrorCode.UNAUTHORIZED_ACCESS, "User not found"));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(()-> cartRepository.save(
                        Cart.builder()
                                .user(user)
                                .totalPrice(BigDecimal.ZERO)
                                .build()
                ));
    }

    private void recalculateCartTotal(Cart cart) {
        BigDecimal total = cart.getCartItems().stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }

    private CartResponseDto mapToCartResponseDto(Cart cart){
        List<CartItemResponseDto> itemDtos = cart.getCartItems().stream()
                .map(item -> CartItemResponseDto.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subTotal(item.getSubTotal())
                        .build()
                )
                .toList();

        int totalCount = itemDtos.stream()
                .mapToInt(CartItemResponseDto::getQuantity)
                .sum();

        return CartResponseDto.builder()
                .id(cart.getId())
                .items(itemDtos)
                .totalItems(totalCount)
                .totalPrice(cart.getTotalPrice())
                .build();
    }
}
