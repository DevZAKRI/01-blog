package com.zerooneblog.blog.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.service.PostService;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedController {
    private final PostService postService;
    private final UserRepository userRepository;
    private final com.zerooneblog.blog.service.UserService userService;

    public FeedController(PostService postService, UserRepository userRepository, com.zerooneblog.blog.service.UserService userService) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public org.springframework.data.domain.Page<com.zerooneblog.blog.dto.response.PostDto> feed(Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (auth == null || auth.getName() == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        User u = userRepository.findByUsername(auth.getName()).orElseThrow();
        return postService.feedFor(u, PageRequest.of(page, size)).map(com.zerooneblog.blog.mapper.EntityMapper::toDto);
    }

    @GetMapping("/{authorId}")
    public org.springframework.data.domain.Page<com.zerooneblog.blog.dto.response.PostDto> feedForAuthor(@PathVariable Long authorId, Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (auth == null || auth.getName() == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        User u = userRepository.findByUsername(auth.getName()).orElseThrow();
        return userService.listPostsForSubscription(u, authorId, PageRequest.of(page, size)).map(com.zerooneblog.blog.mapper.EntityMapper::toDto);
    }
}
