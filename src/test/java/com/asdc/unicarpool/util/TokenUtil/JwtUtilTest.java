package com.asdc.unicarpool.util.TokenUtil;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;
    private static final String TEST_SECRET = "testSecretKeyForJwtTokenGenerationAndValidation1234567890";
    private static final Long TEST_EXPIRATION = 3600L; // 1 hour in seconds
    private static final String TEST_BANNER_ID = "B00123456";
    private static final String TEST_EMAIL = "test@dal.ca";
    private static final String TEST_NAME = "Test User";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);

        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.RIDER);

        testUser = User.builder()
                .id(1L)
                .bannerId(TEST_BANNER_ID)
                .email(TEST_EMAIL)
                .name(TEST_NAME)
                .phoneNumber("1234567890")
                .password("password")
                .emailVerified(true)
                .roles(roles)
                .build();
    }

    @Test
    void testGenerateToken_Success() {
        String token = jwtUtil.generateToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void testGenerateToken_ContainsCorrectClaims() {
        String token = jwtUtil.generateToken(testUser);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(TEST_BANNER_ID, claims.getSubject());
        assertEquals(TEST_BANNER_ID, claims.get(AppConstant.JWT_BANNER_ID));
        assertEquals(TEST_EMAIL, claims.get(AppConstant.JWT_EMAIL));
        assertEquals(TEST_NAME, claims.get(AppConstant.JWT_NAME));
        assertNotNull(claims.get(AppConstant.JWT_ROLES));
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = jwtUtil.generateToken(testUser);

        Boolean isValid = jwtUtil.validateToken(token, TEST_BANNER_ID);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUsername() {
        String token = jwtUtil.generateToken(testUser);

        Boolean isValid = jwtUtil.validateToken(token, "WRONG_BANNER_ID");

        assertFalse(isValid);
    }

    @Test
    void testExtractBannerId_Success() {
        String token = jwtUtil.generateToken(testUser);

        String extractedBannerId = jwtUtil.extractBannerId(token);

        assertEquals(TEST_BANNER_ID, extractedBannerId);
    }

    @Test
    void testExtractExpiration_Success() {
        String token = jwtUtil.generateToken(testUser);

        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testIsTokenExpired_NotExpired() {
        String token = jwtUtil.generateToken(testUser);

        Boolean isExpired = jwtUtil.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_ExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L); // Set negative expiration
        String token = jwtUtil.generateToken(testUser);

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.isTokenExpired(token));
    }

    @Test
    void testExtractClaim_CustomClaim() {
        String token = jwtUtil.generateToken(testUser);

        String email = jwtUtil.extractClaim(token, claims -> claims.get(AppConstant.JWT_EMAIL, String.class));

        assertEquals(TEST_EMAIL, email);
    }

    @Test
    void testExtractClaim_IssuedAt() {
        String token = jwtUtil.generateToken(testUser);

        Date issuedAt = jwtUtil.extractClaim(token, Claims::getIssuedAt);

        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
    }

    @Test
    void testGetExpirationTime() {
        Long expirationTime = jwtUtil.getExpirationTime();

        assertEquals(TEST_EXPIRATION, expirationTime);
    }

    @Test
    void testGenerateToken_WithMultipleRoles() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.RIDER);
        roles.add(UserRole.DRIVER);
        testUser.setRoles(roles);

        String token = jwtUtil.generateToken(testUser);

        assertNotNull(token);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(claims.get(AppConstant.JWT_ROLES));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String token = jwtUtil.generateToken(testUser);

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(token, TEST_BANNER_ID));
    }

    @Test
    void testGenerateToken_WithEmptyRoles() {
        testUser.setRoles(new HashSet<>());

        String token = jwtUtil.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractClaim_SubjectMatchesBannerId() {
        String token = jwtUtil.generateToken(testUser);

        String subject = jwtUtil.extractClaim(token, Claims::getSubject);
        String bannerId = jwtUtil.extractBannerId(token);

        assertEquals(subject, bannerId);
        assertEquals(TEST_BANNER_ID, subject);
    }
}
