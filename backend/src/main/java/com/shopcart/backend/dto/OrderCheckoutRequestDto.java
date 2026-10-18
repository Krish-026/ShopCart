package com.shopcart.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCheckoutRequestDto {
    @NotNull(message = "Address ID is required for delivery")
    private Long addressId;
}
