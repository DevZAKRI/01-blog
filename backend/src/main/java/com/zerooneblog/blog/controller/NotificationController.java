package com.zerooneblog.blog.controller;

import java.util.logging.Logger;

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
    private static final Logger logger = Logger.getLogger(NotificationController.class.getName());
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found: " + auth.getName()));
    }

    @GetMapping
    public org.springframework.http.ResponseEntity<?> list(Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        logger.info("[NotificationController] GET /notifications - Listing notifications - page: " + page + ", size: " + size);
        try {
            var user = currentUser(auth);
            logger.fine("[NotificationController] User: " + user.getUsername());
            var result = notificationService.list(user, PageRequest.of(page, size)).map(EntityMapper::toDto);
            logger.info("[NotificationController] Notifications listed successfully - Total: " + result.getTotalElements() + ", Current page: " + result.getContent().size());
            if (result.getTotalElements() == 0) {
                return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                    "message", "No notifications yet",
                    "content", result.getContent(),
                    "totalElements", result.getTotalElements()
                ));
            }
            return org.springframework.http.ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.severe("[NotificationController] Error listing notifications: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, Authentication auth) {
        logger.info("[NotificationController] POST /notifications/{id}/read - Marking notification ID: " + id + " as read");
        try {
            var user = currentUser(auth);
            notificationService.markRead(id, user, true);
            logger.info("[NotificationController] Notification marked as read successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.severe("[NotificationController] Error marking notification as read: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{id}/unread")
    public ResponseEntity<?> markUnread(@PathVariable Long id, Authentication auth) {
        logger.info("[NotificationController] POST /notifications/{id}/unread - Marking notification ID: " + id + " as unread");
        try {
            var user = currentUser(auth);
            notificationService.markRead(id, user, false);
            logger.info("[NotificationController] Notification marked as unread successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.severe("[NotificationController] Error marking notification as unread: " + e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<java.util.Map<String, Long>> unreadCount(Authentication auth) {
        logger.info("[NotificationController] GET /notifications/unread-count - Getting unread count");
        try {
            var user = currentUser(auth);
            long count = notificationService.countUnread(user);
            logger.info("[NotificationController] Unread count: " + count);
            return ResponseEntity.ok(java.util.Map.of("unreadCount", count));
        } catch (Exception e) {
            logger.severe("[NotificationController] Error getting unread count: " + e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        var user = currentUser(auth);
        notificationService.markAllRead(user);
        return ResponseEntity.ok().build();
    }
}
