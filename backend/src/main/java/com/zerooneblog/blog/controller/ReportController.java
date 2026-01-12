package com.zerooneblog.blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.dto.request.ReportRequest;
import com.zerooneblog.blog.dto.response.ReportDto;
import com.zerooneblog.blog.mapper.EntityMapper;
import com.zerooneblog.blog.model.Report;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.service.ReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class ReportController {
    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) { 
        return userRepository.findByEmail(auth.getName()).orElseThrow(); 
    }

    @PostMapping("/users/{id}/report")
    public ResponseEntity<ReportDto> reportUser(@PathVariable Long id, @Valid @RequestBody ReportRequest req, Authentication auth) {
        Report r = reportService.reportUser(id, currentUser(auth), req.getReason());
        return ResponseEntity.ok(EntityMapper.toDto(r));
    }

    @PostMapping("/posts/{id}/report")
    public ResponseEntity<ReportDto> reportPost(@PathVariable Long id, @Valid @RequestBody ReportRequest req, Authentication auth) {
        Report r = reportService.reportPost(id, currentUser(auth), req.getReason());
        return ResponseEntity.ok(EntityMapper.toDto(r));
    }
}
