package com.zerooneblog.blog.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.mapper.EntityMapper;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return userRepository.findByUsername(auth.getName()).orElseThrow();
    }

    @GetMapping
    public org.springframework.http.ResponseEntity<?> list(Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var result = notificationService.list(currentUser(auth), PageRequest.of(page, size)).map(EntityMapper::toDto);
        if (result.getTotalElements() == 0) {
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                "message", "No notifications yet",
                "content", result.getContent(),
                "totalElements", result.getTotalElements()
            ));
        }
        return org.springframework.http.ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, Authentication auth) {
        // lightweight: mark read if belongs to user
        var user = currentUser(auth);
        notificationService.markRead(id, user, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unread")
    public ResponseEntity<?> markUnread(@PathVariable Long id, Authentication auth) {
        var user = currentUser(auth);
        notificationService.markRead(id, user, false);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<java.util.Map<String, Long>> unreadCount(Authentication auth) {
        var user = currentUser(auth);
        long count = notificationService.countUnread(user);
        return ResponseEntity.ok(java.util.Map.of("unread", count));
    }
    
    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        var user = currentUser(auth);
        notificationService.markAllRead(user);
        return ResponseEntity.ok().build();
    }
}
