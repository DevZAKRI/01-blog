package com.zerooneblog.blog.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractJwtFromRequest(request);

        if (token != null && jwtUtil.isValidToken(token)) {
            String email = jwtUtil.getEmailFromToken(token);
            Long tokenVersion = jwtUtil.getTokenVersion(token);
            
            // Check if user exists, is not banned, and token version matches
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Check if user is banned
                if (user.isBanned()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Your account has been banned\",\"code\":\"ACCOUNT_BANNED\"}");
                    return;
                }
                
                // Check token version - if user's token version is higher, token is invalidated
                Long userTokenVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0L;
                if (tokenVersion < userTokenVersion) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Session expired. Please log in again.\",\"code\":\"TOKEN_INVALIDATED\"}");
                    return;
                }
                
                var auth = jwtUtil.getAuthentication(token);
                System.out.println("[JWT Filter] Token valid, username from token: '" + auth.getName() + "' (length: " + (auth.getName() != null ? auth.getName().length() : "null") + ")");
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // extract token from "Bearer <token>"
        }
        return null;
    }
}
