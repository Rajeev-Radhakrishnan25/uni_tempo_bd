package com.asdc.unicarpool.component;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.exception.InvalidTokenException;
import com.asdc.unicarpool.util.TokenUtil.ITokenUtil;
import com.asdc.unicarpool.util.TokenUtil.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenExtractor {

    private final ITokenUtil jwtUtil;

    @Autowired
    public TokenExtractor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String extractBannerIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AppConstant.HEADER_AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(AppConstant.HEADER_BEARER_PREFIX)) {
            throw new InvalidTokenException("Authorization header is missing or invalid");
        }

        String token = authHeader.substring(AppConstant.HEADER_BEARER_PREFIX.length());
        try {
            return jwtUtil.extractBannerId(token);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired JWT token");
        }
    }
}
