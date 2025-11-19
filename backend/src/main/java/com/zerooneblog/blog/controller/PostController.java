package com.zerooneblog.blog.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.dto.response.PostDto;
import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.mapper.EntityMapper;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.PostRepository;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.service.PostService;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostController(PostService postService, UserRepository userRepository, PostRepository postRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName()).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @PostMapping
    public ResponseEntity<PostDto> create(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.zerooneblog.blog.dto.request.CreatePostRequest req, Authentication auth) {
        User u = currentUser(auth);
        Post p = new Post();
        p.setAuthor(u);
        p.setDescription(req.getDescription());
        p.setMediaUrl(req.getMediaUrl());
        Post saved = postService.create(p);
        return ResponseEntity.created(URI.create("/api/v1/posts/" + saved.getId())).body(EntityMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public PostDto edit(@PathVariable Long id, @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.zerooneblog.blog.dto.request.UpdatePostRequest req, Authentication auth) {
        User u = currentUser(auth);
        Post p = new Post();
        p.setDescription(req.getDescription());
        p.setMediaUrl(req.getMediaUrl());
        Post updated = postService.edit(id, p, u);
        return EntityMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        User u = currentUser(auth);
        postService.delete(id, u);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public PostDto get(@PathVariable Long id) { return EntityMapper.toDto(postService.getById(id)); }

    @GetMapping
    public Page<PostDto> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return postRepository.findAll(PageRequest.of(page, size)).map(EntityMapper::toDto);
    }
}
