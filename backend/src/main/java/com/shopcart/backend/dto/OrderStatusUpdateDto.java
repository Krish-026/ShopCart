package com.shopcart.backend.dto;

import com.shopcart.backend.entity.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusUpdateDto {

    @NotBlank(message = "Order status is required")
    private OrderStatus status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}
