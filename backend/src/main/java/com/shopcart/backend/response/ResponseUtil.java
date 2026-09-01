package com.shopcart.backend.response;

import com.shopcart.backend.exception.ErrorCode;

public class ResponseUtil {

    // ==========================================
    // SUCCESS RESPONSES
    // ==========================================
    public static <T> ApiResponse<T> success(T data, String message){
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data){
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operation successful")
                .data(data)
                .build();
    }


    // ==========================================
    // ERROR RESPONSES
    // ==========================================

    public static <T> ApiResponse<T> error(String message){
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }


    public static <T> ApiResponse<T> error(ErrorCode errorCode){
        return ApiResponse.<T>builder()
                .success(false)
                .message(errorCode.getDefaultMessage())
                .errorCode(errorCode.getCode())
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage){
        return ApiResponse.<T>builder()
                .success(false)
                .message(customMessage)
                .errorCode(errorCode.getCode())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}
