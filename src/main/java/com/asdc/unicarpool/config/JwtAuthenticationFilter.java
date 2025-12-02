package com.asdc.unicarpool.config;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.exception.InvalidTokenException;
import com.asdc.unicarpool.util.TokenUtil.ITokenUtil;
import com.asdc.unicarpool.util.TokenUtil.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ITokenUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AppConstant.HEADER_AUTHORIZATION);
        String bannerId = null;
        String jwt = null;
        if (authHeader != null && authHeader.startsWith(AppConstant.HEADER_BEARER_PREFIX)) {
            jwt = authHeader.substring(AppConstant.HEADER_BEARER_PREFIX.length());
            try {
                bannerId = jwtUtil.extractBannerId(jwt);
            } catch (Exception e) {
                logger.error("JWT Token extraction failed: " + e.getMessage());
                throw new InvalidTokenException("JWT Token extraction failed: " + e.getMessage());
            }
        }
        if (bannerId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(bannerId);
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

}
