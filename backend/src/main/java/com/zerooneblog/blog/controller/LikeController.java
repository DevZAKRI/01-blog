package com.zerooneblog.blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.service.LikeService;

@RestController
@RequestMapping("/api/v1/posts")
public class LikeController {
    private final LikeService likeService;
    private final UserRepository userRepository;

    public LikeController(LikeService likeService, UserRepository userRepository) {
        this.likeService = likeService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found: " + auth.getName()));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<java.util.Map<String, Object>> toggleLike(@PathVariable Long id, Authentication auth) {
        boolean liked = likeService.toggleLike(id, currentUser(auth));
        return ResponseEntity.ok(java.util.Map.of("liked", liked));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlike(@PathVariable Long id, Authentication auth) {
        likeService.unlike(id, currentUser(auth));
        return ResponseEntity.noContent().build();
    }
}
