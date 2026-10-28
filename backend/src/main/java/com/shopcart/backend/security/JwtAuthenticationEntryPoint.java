package com.shopcart.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.backend.exception.ErrorCode;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Void> errorResponse = ResponseUtil.error(
                ErrorCode.UNAUTHORIZED_ACCESS,
                "Authentication required. Please provide a valid Bearer token."
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
