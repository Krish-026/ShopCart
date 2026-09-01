package com.shopcart.backend.exception;

import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex){
        ErrorCode errorCode = ex.getErrorCode();

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(errorCode.getCode()) // e.g. "CAT_001"
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    // Handle custom Business Exceptions like "Email already registered"
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {

        log.warn("Business rule violation: {}", ex.getMessage());
        return new ResponseEntity<>(
                ResponseUtil.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    // Handle DTO Form Validation Errors (like missing fields, bad passwords)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Loop through all failed fields and grab their error messages
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed for request: {}", errors);

        return new ResponseEntity<>(
                ResponseUtil.error("Input validation failed", errors),
                HttpStatus.BAD_REQUEST
        );
    }

    // The Ultimate Catch-All for Server Crashes(NullPointers, DB Connections, etc)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {

        // Print the entire stack trace to debug it
        log.error("Critical Server Error occurred: ", ex);

        return new ResponseEntity<>(
                ResponseUtil.error("An unexpected internal server error occurred. Please try again later."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
