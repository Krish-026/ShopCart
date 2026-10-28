package com.shopcart.backend.controller;

import com.shopcart.backend.dto.PaymentRequestDto;
import com.shopcart.backend.dto.PaymentResponseDto;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.ResponseUtil;
import com.shopcart.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payment Management", description = "Endpoint for processing checkout payments and viewing receipts")
public class PaymentController {

    private final PaymentService paymentService;
    @PostMapping("/process")
    @Operation(summary = "Process or simulate payment for a pending order")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> processPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentRequestDto request
            ){

        PaymentResponseDto response = paymentService.processPayment(userDetails.getUsername(), request);

        return ResponseEntity.ok(
                ResponseUtil.success(response, "Payment processed")
        );
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Retrieve payment receipt by Order ID")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentByOrderId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId
    ){

        PaymentResponseDto response = paymentService.getPaymentByOrderId(userDetails.getUsername(), orderId);

        return ResponseEntity.ok(
                ResponseUtil.success(response, "Payment details fetched")
        );
    }
}