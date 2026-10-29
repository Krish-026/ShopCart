package com.shopcart.backend.controller;

import com.shopcart.backend.dto.OrderCheckoutRequestDto;
import com.shopcart.backend.dto.OrderResponseDto;
import com.shopcart.backend.dto.OrderStatusUpdateDto;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.PageResponse;
import com.shopcart.backend.response.ResponseUtil;
import com.shopcart.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "order-controller", description = "Order and Checkout APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Checkout cart and place an order")
    public ResponseEntity<ApiResponse<OrderResponseDto>> checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderCheckoutRequestDto request
            ){

        OrderResponseDto order = orderService.checkout(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(order, "Order placed successfully"));
    }

    @GetMapping
    @Operation(summary = "Get current logged-in user order history (Paginated)")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponseDto>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir

    ){
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        PageResponse<OrderResponseDto> orders = orderService.getUserOrders(userDetails.getUsername(),pageable);

        return ResponseEntity.ok(ResponseUtil.success(orders, "Order history retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific order details by ID")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ){
        OrderResponseDto order = orderService.getOrderById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ResponseUtil.success(
                order,
                "Order details retrieved successfully"
        ));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin : Get all platform orders (Paginated)")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponseDto>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ){
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        PageResponse<OrderResponseDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ResponseUtil.success(orders, "All platform orders retrieved successfully"));
    }

    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Update order fulfillment status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDto request
            ){
        OrderResponseDto order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ResponseUtil.success(order, "Order status updated successfully"));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Customer: Cancel an order before shipment")
    public ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ){
        OrderResponseDto order = orderService.cancelOrder(userDetails.getUsername(), id);

        return ResponseEntity.ok(ResponseUtil.success(order, "Order cancelled successfully"));
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir){
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}
