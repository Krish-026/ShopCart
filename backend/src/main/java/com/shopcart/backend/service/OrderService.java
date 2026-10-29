package com.shopcart.backend.service;

import com.shopcart.backend.dto.OrderCheckoutRequestDto;
import com.shopcart.backend.dto.OrderResponseDto;
import com.shopcart.backend.dto.OrderStatusUpdateDto;
import com.shopcart.backend.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponseDto checkout(String userEmail, OrderCheckoutRequestDto request);
    PageResponse<OrderResponseDto> getUserOrders(String userEmail, Pageable pageable);
    OrderResponseDto getOrderById(String userEmail, Long orderId);
    PageResponse<OrderResponseDto> getAllOrders(Pageable pageable);
    OrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto request);
    OrderResponseDto cancelOrder(String userEmail, Long orderId);
}
