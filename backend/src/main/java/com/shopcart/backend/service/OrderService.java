package com.shopcart.backend.service;

import com.shopcart.backend.dto.OrderCheckoutRequestDto;
import com.shopcart.backend.dto.OrderResponseDto;
import com.shopcart.backend.dto.OrderStatusUpdateDto;

import java.util.List;

public interface OrderService {
    OrderResponseDto checkout(String userEmail, OrderCheckoutRequestDto request);
    List<OrderResponseDto> getUserOrders(String userEmail);
    OrderResponseDto getOrderById(String userEmail, Long orderId);
    List<OrderResponseDto> getAllOrders();
    OrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto request);
}
