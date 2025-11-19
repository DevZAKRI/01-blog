package com.zerooneblog.blog.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.repository.PostRepository;
import com.zerooneblog.blog.repository.ReportRepository;
import com.zerooneblog.blog.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;

    public AdminController(UserRepository userRepository, PostRepository postRepository, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.reportRepository = reportRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public org.springframework.data.domain.Page<com.zerooneblog.blog.dto.response.UserDto> users(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return userRepository.findAll(PageRequest.of(page, size)).map(com.zerooneblog.blog.mapper.EntityMapper::toDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/posts")
    public org.springframework.data.domain.Page<com.zerooneblog.blog.dto.response.PostDto> posts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return postRepository.findAll(PageRequest.of(page, size)).map(com.zerooneblog.blog.mapper.EntityMapper::toDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports")
    public org.springframework.data.domain.Page<com.zerooneblog.blog.dto.response.ReportDto> reports(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return reportRepository.findAll(PageRequest.of(page, size)).map(com.zerooneblog.blog.mapper.EntityMapper::toDto);
    }
}
