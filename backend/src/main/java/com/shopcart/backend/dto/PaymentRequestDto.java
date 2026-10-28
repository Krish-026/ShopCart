package com.shopcart.backend.dto;

import com.shopcart.backend.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Optional simulated card/UPI details or simulate failure flag
    private boolean simulateFailure;
}
