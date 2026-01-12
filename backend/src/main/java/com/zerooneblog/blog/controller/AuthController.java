package com.zerooneblog.blog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.zerooneblog.blog.dto.request.LoginRequest;
import com.zerooneblog.blog.dto.request.RegisterRequest;
import com.zerooneblog.blog.dto.response.AuthResponse;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.util.HtmlSanitizer;
import com.zerooneblog.blog.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final HtmlSanitizer htmlSanitizer;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          HtmlSanitizer htmlSanitizer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.htmlSanitizer = htmlSanitizer;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest body) {
        // Case-insensitive duplicate checks
        if (userRepository.existsByUsernameIgnoreCase(body.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(body.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
        }

        User user = new User();
        user.setUsername(htmlSanitizer.sanitizePlainText(body.getUsername()));
        user.setPassword(passwordEncoder.encode(body.getPassword()));
        user.setEmail(body.getEmail());
        user.setTokenVersion(0L);
        // Default role is USER (set in entity)

        userRepository.save(user);

        // Generate token with email, role, and token version
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getTokenVersion());
        return ResponseEntity.ok(new AuthResponse(token, com.zerooneblog.blog.mapper.EntityMapper.toDto(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
        // Support login with either username or email
        String identifier = body.getUsername();
        User user;
        
        // Check if identifier is an email (contains @)
        if (identifier.contains("@")) {
            user = userRepository.findByEmailIgnoreCase(identifier)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        } else {
            user = userRepository.findByUsernameIgnoreCase(identifier)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        }
        
        // Check if user is banned
        if (user.isBanned()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account has been banned");
        }
        
        try {
            // Authenticate using the actual username from database
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), body.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // Generate token with email, role, and token version
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getTokenVersion());
        return ResponseEntity.ok(new AuthResponse(token, com.zerooneblog.blog.mapper.EntityMapper.toDto(user)));
    }
}
