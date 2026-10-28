package com.shopcart.backend.dto;

import com.shopcart.backend.entity.OrderStatus;
import com.shopcart.backend.entity.PaymentMethod;
import com.shopcart.backend.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponseDto {

    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private OrderStatus orderStatus;
    private LocalDateTime paymentDate;
}
