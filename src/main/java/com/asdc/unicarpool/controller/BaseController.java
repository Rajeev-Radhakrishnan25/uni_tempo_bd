package com.asdc.unicarpool.controller;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.exception.InvalidTokenException;
import com.asdc.unicarpool.util.TokenUtil.ITokenUtil;
import com.asdc.unicarpool.util.TokenUtil.JwtUtilI;
import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseController {

    private final ITokenUtil jwtUtil;

    public BaseController(JwtUtilI jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    protected String extractBannerIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AppConstant.Headers.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(AppConstant.Headers.BEARER_PREFIX)) {
            throw new InvalidTokenException("Authorization header is missing or invalid");
        }

        String token = authHeader.substring(7);
        try {
            return jwtUtil.extractBannerId(token);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired JWT token");
        }
    }
}
