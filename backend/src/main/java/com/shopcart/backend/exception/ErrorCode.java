package com.shopcart.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth & Security
    UNAUTHORIZED_ACCESS("AUTH_001", "Unauthorized access", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("AUTH_002", "Invalid username or password", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_003", "JWT token has expired", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("AUTH_004", "User not found", HttpStatus.NOT_FOUND),

    // Category Domain
    CATEGORY_NOT_FOUND("CAT_001", "Category not found", HttpStatus.NOT_FOUND),
    DUPLICATE_CATEGORY_NAME("CAT_002", "Category name already exists", HttpStatus.CONFLICT),


    // Product Domain
    PRODUCT_NOT_FOUND("PROD_001", "Product not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("PROD_002", "Product stock is insufficient", HttpStatus.BAD_REQUEST),

    // Cart & Order Domain
    CART_NOT_FOUND("CART_001", "Cart not found", HttpStatus.NOT_FOUND),
    CART_EMPTY("CART_002", "Cart is empty", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND("ORD_001", "Order not found", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS("ORD_002", "Operation not allowed for current order status", HttpStatus.BAD_REQUEST),

    // Payment Domain
    RESOURCE_NOT_FOUND("SYS_000", "Request resource not found", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND("PAY_001", "No payment record found", HttpStatus.NOT_FOUND),
    PAYMENT_FAILED("PAY_002", "Payment processing failed", HttpStatus.BAD_REQUEST),

    // General
    INTERNAL_SERVER_ERROR("SYS_001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT("SYS_002", "Validation failed for request parameters", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus){
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}
