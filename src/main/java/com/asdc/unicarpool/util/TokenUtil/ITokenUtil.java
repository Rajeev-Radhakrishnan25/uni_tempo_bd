package com.asdc.unicarpool.util.TokenUtil;

import com.asdc.unicarpool.model.User;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

public interface ITokenUtil {

    public String generateToken(User user);

    public Boolean validateToken(String token, String username);

    public Boolean isTokenExpired(String token);

    public String extractBannerId(String token);

    public Date extractExpiration(String token);

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    public Long getExpirationTime();
}
