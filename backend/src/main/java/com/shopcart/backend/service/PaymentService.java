package com.shopcart.backend.service;

import com.shopcart.backend.dto.PaymentRequestDto;
import com.shopcart.backend.dto.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto processPayment(String userEmail, PaymentRequestDto request);
    PaymentResponseDto getPaymentByOrderId(String userEmail, Long orderId);
}
