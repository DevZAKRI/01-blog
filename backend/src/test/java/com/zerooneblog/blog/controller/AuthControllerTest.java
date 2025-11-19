package com.zerooneblog.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerooneblog.blog.dto.request.LoginRequest;
import com.zerooneblog.blog.dto.request.RegisterRequest;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void registerReturnsToken() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("secret123");
        req.setEmail("alice@example.com");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(jwtUtil.generateToken(any())).thenReturn("token-value");

    mockMvc.perform(post("/api/v1/auth/register").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-value"));
    }

    @Test
    void loginReturnsToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("secret123");

    // mock authenticationManager to return an Authentication on authenticate
    when(authenticationManager.authenticate(any())).thenReturn(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("alice", null, java.util.Collections.emptyList()));
    when(jwtUtil.generateToken(any())).thenReturn("token-login");

    mockMvc.perform(post("/api/v1/auth/login").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-login"));
    }
}
