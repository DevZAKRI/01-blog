package com.zerooneblog.blog.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // NOTE: use a sufficiently long secret in production and keep it out of source control
    private static final String SECRET_KEY = "change-this-to-a-very-long-secret-key-with-at-least-64-bytes-length-1234567890";
    private static final long EXPIRATION_TIME = 86400000L; // 1 day

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    /**
     * Generate JWT token with user email, role, and token version
     */
    public String generateToken(String email, String role, Long tokenVersion) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("tokenVersion", tokenVersion != null ? tokenVersion : 0L)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Generate JWT token (backward compatible - uses tokenVersion 0)
     */
    public String generateToken(String email, String role) {
        return generateToken(email, role, 0L);
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getTokenVersion(String token) {
        try {
            Claims claims = getClaims(token);
            Object version = claims.get("tokenVersion");
            if (version instanceof Number) {
                return ((Number) version).longValue();
            }
            return 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        
        // Create authorities with ROLE_ prefix for Spring Security
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER"))
        );
        
        return new UsernamePasswordAuthenticationToken(email, null, authorities);
    }
}