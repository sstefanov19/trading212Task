package org.example.tasktrading212.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGtleSBmb3Igand0IHRlc3Rpbmc="; // test secret
    private static final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET", SECRET);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateToken(USERNAME);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(USERNAME);

        String extracted = jwtUtil.extractUsername(token);

        assertEquals(USERNAME, extracted);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken(USERNAME);
        UserDetails userDetails = new User(USERNAME, "password", Collections.emptyList());

        assertTrue(jwtUtil.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalseForWrongUsername() {
        String token = jwtUtil.generateToken(USERNAME);
        UserDetails userDetails = new User("otheruser", "password", Collections.emptyList());

        assertFalse(jwtUtil.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldThrowForExpiredToken() {
        // Create a token that's already expired
        String expiredToken = Jwts.builder()
                .claims(new HashMap<>())
                .subject(USERNAME)
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(SECRET)))
                .compact();

        UserDetails userDetails = new User(USERNAME, "password", Collections.emptyList());

        assertThrows(ExpiredJwtException.class, () ->
                jwtUtil.isTokenValid(expiredToken, userDetails)
        );
    }

    @Test
    void extractAllClaims_shouldThrowForInvalidToken() {
        assertThrows(Exception.class, () ->
                jwtUtil.extractAllClaims("invalid.token.here")
        );
    }

    @Test
    void generateToken_shouldContainIssuedAtAndExpiration() {
        String token = jwtUtil.generateToken(USERNAME);

        var claims = jwtUtil.extractAllClaims(token);

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }
}