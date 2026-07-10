package com.shopcart.backend.controller;

import com.shopcart.backend.dto.UserRegistrationDto;
import com.shopcart.backend.entity.User;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.ResponseUtil;
import com.shopcart.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody UserRegistrationDto request) {
        User response = userService.registerUser(request);

        return new ResponseEntity<>(ResponseUtil.success(
                response, "User registered successfully"),
                HttpStatus.CREATED
        );
    }
}
