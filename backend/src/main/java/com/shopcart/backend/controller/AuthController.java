package com.shopcart.backend.controller;

import com.shopcart.backend.dto.AuthResponseDto;
import com.shopcart.backend.dto.LoginRequestDto;
import com.shopcart.backend.entity.User;
import com.shopcart.backend.repository.UserRepository;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.ResponseUtil;
import com.shopcart.backend.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request){
        //1. Authenticate user credentials against BCrypt hash in database
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        //2. Fetch Users from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("User not found: " + request.getEmail()));

        //3. Generate signed JWT token
        String token = jwtUtil.generateToken(user);

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token(token)
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(ResponseUtil.success(authResponse, "Login Successfully"));

    }
}
