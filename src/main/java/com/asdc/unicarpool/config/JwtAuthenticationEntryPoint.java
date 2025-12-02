package com.asdc.unicarpool.config;

import com.asdc.unicarpool.constant.AppConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        final ObjectMapper objectMapper = new ObjectMapper();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(AppConstant.ERROR_TIMESTAMP, Instant.now().toString());
        errorResponse.put(AppConstant.ERROR_STATUS, HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put(AppConstant.ERROR_MESSAGE, "Unauthorized");

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
