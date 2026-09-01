package com.shopcart.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth & Security
    UNAUTHORIZED_ACCESS("AUTH_001", "Unauthorized access", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("AUTH_002", "Invalid username or password", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_003", "JWT token has expired", HttpStatus.UNAUTHORIZED),

    // Category Domain
    CATEGORY_NOT_FOUND("CAT_001", "Category not found", HttpStatus.NOT_FOUND),
    DUPLICATE_CATEGORY_NAME("CAT_002", "Category name already exists", HttpStatus.CONFLICT),


    // Product Domain
    PRODUCT_NOT_FOUND("PROD_001", "Product not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("PROD_002", "Product stock is insufficient", HttpStatus.BAD_REQUEST),

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
