package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.OrderCheckoutRequestDto;
import com.shopcart.backend.dto.OrderItemResponseDto;
import com.shopcart.backend.dto.OrderResponseDto;
import com.shopcart.backend.dto.OrderStatusUpdateDto;
import com.shopcart.backend.entity.*;
import com.shopcart.backend.exception.BadRequestException;
import com.shopcart.backend.exception.ErrorCode;
import com.shopcart.backend.exception.ResourceNotFoundException;
import com.shopcart.backend.repository.*;
import com.shopcart.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    @Transactional
    public OrderResponseDto checkout(String userEmail, OrderCheckoutRequestDto request) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.UNAUTHORIZED_ACCESS, "User not found"));

        // 1. Fetch and format the selected shipping address
        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Address not found or does not belong to user"));

        String formattedAddress = String.format(
                "%s, %s, %s, %s - %s, %s | Phone: %s",
                address.getFullName(),
                address.getStreetAddress(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getPhoneNumber()
        );

        // 2. Fetch User's Cart
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Cart is empty. Add items before checkout"));

        // 3. Initialize Order
        Order order = Order.builder()
                .user(user)
                .shippingAddress(formattedAddress)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 4. Validate stock with Pessimistic lock, decrement inventory, and build OrderItems
        for (CartItem cartItem : cart.getCartItems()) {
            // Product product = cartItem.getProduct();
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorCode.PRODUCT_NOT_FOUND,
                            "Product not found with ID: " + cartItem.getProduct().getId()
                    ));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new ResourceNotFoundException(ErrorCode.INSUFFICIENT_STOCK, "Insufficient stock for product: " + product.getName() + ". Available: " + product.getStockQuantity());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Create OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .subTotal(cartItem.getSubTotal())
                    .build();

            order.getOrderItems().add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubTotal());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // 5. Clear cart
        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);


        return mapToOrderResponseDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getUserOrders(String userEmail) {
        // 1. Find User, or throw error
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.UNAUTHORIZED_ACCESS, "User not found"));
        // 2. Find User's order or throw error
        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        // 3. Set response
        return userOrders.stream()
                .map(this::mapToOrderResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDto getOrderById(String userEmail, Long orderId) {
        // 1. Find User, or throw error
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.UNAUTHORIZED_ACCESS, "User not found"));

        // 2. Find order by id
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Order not found with ID: " + orderId));

        // 3. Return order
        return mapToOrderResponseDto(order);
    }

    @Override
    public List<OrderResponseDto> getAllOrders() {
        List<Order> orderList = orderRepository.findAll();
        return orderList.stream()
                .map(this::mapToOrderResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto request) {
        // 1. Validate order
        // 2. Update status

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Order not found with ID: " + orderId));

        OrderStatus newStatus = request.getStatus();

        if (!order.getOrderStatus().canTransitionTo(newStatus)) {
            throw new BadRequestException(ErrorCode.INVALID_INPUT, "Invalid status transition from " + order.getOrderStatus() + " to " + newStatus);
        }

        // If an admin cancels the order, restore stock back to products
        if (newStatus == OrderStatus.CANCELLED) {
            restoreInventory(order);
        }

        order.setOrderStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponseDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(String userEmail, Long orderId) {
        // 1. Check user
        // 2. Check order
        // 3. Status update
        // 4. Inventory update
        // 5. Save
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.UNAUTHORIZED_ACCESS, "User not found"));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Order not found with ID: " + orderId));

        if (!order.getOrderStatus().canTransitionTo(OrderStatus.CANCELLED)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_INPUT,
                    "Order cannot be cancelled in its current state: " + order.getOrderStatus()
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        restoreInventory(order);

        Order updateOrder = orderRepository.save(order);

        return mapToOrderResponseDto(updateOrder);
    }

    private OrderResponseDto mapToOrderResponseDto(Order order) {
        List<OrderItemResponseDto> itemResponseDtos = order.getOrderItems().stream()
                .map(item -> OrderItemResponseDto.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subTotal(item.getSubTotal())
                        .build()
                )
                .collect(Collectors.toList());


        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .shippingAddress(order.getShippingAddress())
                .orderItems(itemResponseDtos)
                .build();
    }

    private void restoreInventory(Order order) {
        for (OrderItem item : order.getOrderItems()) {
//            Product product = item.getProduct();
            Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorCode.PRODUCT_NOT_FOUND,
                            "Product not found with ID: " + item.getProduct().getId()
                    ));
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }
}
