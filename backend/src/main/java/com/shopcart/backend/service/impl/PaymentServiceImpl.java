package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.PaymentRequestDto;
import com.shopcart.backend.dto.PaymentResponseDto;
import com.shopcart.backend.entity.*;
import com.shopcart.backend.exception.BadRequestException;
import com.shopcart.backend.exception.ErrorCode;
import com.shopcart.backend.exception.ResourceNotFoundException;
import com.shopcart.backend.repository.OrderRepository;
import com.shopcart.backend.repository.PaymentRepository;
import com.shopcart.backend.repository.UserRepository;
import com.shopcart.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(String userEmail, PaymentRequestDto request) {
        // 1. Verify User
        // 2. Verify Order
        // 3. Start payment process
        // 4. After successful transaction update order status
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.UNAUTHORIZED_ACCESS, "User not found"));

        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Order not found with ID: " + request.getOrderId()));

        // Check if order is eligible for payment
        if(order.getOrderStatus() != OrderStatus.PENDING){
            throw new BadRequestException(ErrorCode.INVALID_INPUT, "Payment cannot be processed. Order status is: " + order.getOrderStatus());
        }

        // Retrieve existing payment record(e.g., from a previously failed attempt) or build a new one
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElse(
                        Payment.builder()
                                .order(order)
                                .amount(order.getTotalAmount())
                                .build()
                );
        payment.setPaymentMethod(request.getPaymentMethod());

        // Simulate Gateway Execution
        if(request.isSimulateFailure()){
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setTransactionId("TXN-FAILED-" + UUID.randomUUID().toString().substring(0, 8));
            Payment savedPayment = paymentRepository.save(payment);
            log.warn("Payment failed for order ID: {}", order.getId());
            return mapToResponseDto(savedPayment);
        }

        // Successful Payment Flow
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        Payment savedPayment = paymentRepository.save(payment);

        // Transition Order state to CONFIRM
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        log.info("Payment successful for order ID: {}. Transaction ID: {}", order.getId(), payment.getTransactionId());

        return mapToResponseDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(String userEmail, Long orderId) {
        // 1. Verify User
        // 2. Verify Order
        // 3. Fetch Payment
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.UNAUTHORIZED_ACCESS, "User not found"));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Order not found with ID: " + orderId));

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "No payment record found for order ID: " + orderId));

        return mapToResponseDto(payment);
    }


    private PaymentResponseDto mapToResponseDto(Payment payment){
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .orderStatus(payment.getOrder().getOrderStatus())
                .paymentDate(payment.getUpdatedAt() != null ? payment.getUpdatedAt() : LocalDateTime.now())
                .build();
    }
}
