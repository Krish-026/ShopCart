package com.shopcart.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String shippingAddress;
    private List<OrderItemResponseDto> orderItems;
}
