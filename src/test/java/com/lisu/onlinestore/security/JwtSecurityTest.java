package com.lisu.onlinestore.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class JwtSecurityTest {
    private static final String SECRET = "test-secret-with-at-least-thirty-two-bytes";
    private final JwtUtil jwtUtil = new JwtUtil(SECRET);
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void tokensMustHaveExpirationAndSubject() {
        assertFalse(jwtUtil.isValidToken(Jwts.builder().subject("user@test.com")
                .signWith(key).compact()));
        assertFalse(jwtUtil.isValidToken(Jwts.builder().expiration(future())
                .signWith(key).compact()));
        assertFalse(jwtUtil.isValidToken(Jwts.builder().subject(" ").expiration(future())
                .signWith(key).compact()));
        assertFalse(jwtUtil.isValidToken("broken.token"));
        assertFalse(jwtUtil.isValidToken(Jwts.builder().subject("user@test.com")
                .expiration(new Date(0)).signWith(key).compact()));
        assertTrue(jwtUtil.isValidToken(validToken()));
    }

    @Test
    void deletedUserTokenReturnsUnauthorized() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil,
                username -> {
                    throw new UsernameNotFoundException("Deleted user");
                });
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request(validToken()), response, new MockFilterChain());
        assertEquals(401, response.getStatus());
    }

    @Test
    void disabledUserTokenReturnsUnauthorized() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil,
                username -> new User(username, "password", false, true, true, true, List.of()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request(validToken()), response, new MockFilterChain());
        assertEquals(401, response.getStatus());
    }

    @Test
    void malformedTokenReturnsUnauthorized() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil,
                username -> {
                    throw new AssertionError("Invalid tokens must not load users");
                });
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request("broken.token"), response, new MockFilterChain());
        assertEquals(401, response.getStatus());
    }

    @Test
    void validTokenAuthenticatesUserAndContinuesChain() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil,
                username -> User.withUsername(username).password("password").roles("USER").build());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request(validToken()), response, chain);
        assertEquals("user@test.com", SecurityContextHolder.getContext()
                .getAuthentication().getName());
        assertTrue(chain.getRequest() != null);
    }

    private MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private String validToken() {
        return Jwts.builder().subject("user@test.com").expiration(future())
                .signWith(key).compact();
    }

    private Date future() {
        return new Date(System.currentTimeMillis() + 60000);
    }
}
